package xyz.teamnerds.wordgame.datastore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.teamnerds.wordgame.datastore.model.DbDailyGame;
import xyz.teamnerds.wordgame.datastore.model.DbGameAnswerRecord;
import xyz.teamnerds.wordgame.datastore.model.DbGameAnswerRecord_;
import xyz.teamnerds.wordgame.datastore.model.DbGameInfo;


@Component
public class MySQLWordGameDatastore implements WordGameDatastore
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLWordGameDatastore.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;
    
    @Autowired
    private DbDailyGameRepository dailyGameRepository;
    
    @Autowired
    private DbGameAnswerRecordRepository gameAnswerRecordRepository;
    
    private ExecutorService executor = Executors.newFixedThreadPool(10);
    
    @PreDestroy
    public void shutdown()
    {
        executor.shutdown();
    }

    @CheckForNull
    @Override
    public DbGameInfo getGameForDate(@Nonnull LocalDate date)
    {
        DbDailyGame dailyGameEntry = dailyGameRepository.findById(date).orElse(null);
        if (dailyGameEntry == null)
        {
            return null;
        }
        else
        {
            return DbGameInfo.builder()
                    .gameId(dailyGameEntry.getGameId())
                    .build();
        }
    }
    
    @Override
    @Nonnull
    public List<DbGameAnswerRecord> getAnsweredGameRecords(@Nonnull String gameId)
    {
        List<DbGameAnswerRecord> results = gameAnswerRecordRepository.findAllByGameIdAndExternalUserIdIsNotNull(gameId);
        return results == null ? new ArrayList<>() : results;
    }
    
    @Override
    @Nonnull
    public DbUserScoreInfo getUserScore(@Nonnull String externalUserId)
    {
        EntityManager em = null;
        
        int score = 0;
        try
        {
            em = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            
            CriteriaQuery<Object> selectStatement = cb.createQuery();
            Root<DbGameAnswerRecord> gameAnswerRecordTable = selectStatement.from(DbGameAnswerRecord.class);
            
            // SELECT SUM(score) FROM DbGameAnswerRecord
            Selection<?> selection = cb.sum(gameAnswerRecordTable.get(DbGameAnswerRecord_.score));
            selectStatement.multiselect(selection);
            
            // WHERE externalId = xxx
            Predicate predicate = cb.equal(gameAnswerRecordTable.get(DbGameAnswerRecord_.externalUserId), externalUserId);
            selectStatement.where(predicate);

            // Execute the query
            Object result = em.createQuery(selectStatement).getSingleResult();
            if (result == null)
            {
                score = 0;
            }
            else if (result instanceof Number)
            {
                Number number = (Number)result;
                score = number.intValue();
            }
            else
            {
                LOGGER.warn("Failed to get score for user " + externalUserId + ", Object=" + result + ".  Query expected to return a number.");
                score = 0;
            }
        }
        finally
        {
            if (em != null)
            {
                try
                {
                    em.close();
                }
                catch (Exception ex)
                {
                    LOGGER.info("Failed to close EntityManager.", ex);
                }
            }
        }
        
        return DbUserScoreInfo.builder()
                .externalUserId(externalUserId)
                .totalScore(score)
                .build();
    }
    
    @Nonnull
    @Override
    public List<DbUserScoreInfo> getUserLeaderboards()
    {
        List<DbUserScoreInfo> results = null;
        EntityManager em = null;
        try
        {
            em = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            
            CriteriaQuery<Object[]> selectStatement = cb.createQuery(Object[].class);
            Root<DbGameAnswerRecord> gameAnswerRecordTable = selectStatement.from(DbGameAnswerRecord.class);
            
            // SELECT SUM(score) FROM DbGameAnswerRecord
            Selection<?> selectSumOfScore = cb.sum(gameAnswerRecordTable.get(DbGameAnswerRecord_.score));
            Selection<?> selectExternalUserId = gameAnswerRecordTable.get(DbGameAnswerRecord_.externalUserId);
            selectStatement.multiselect(selectExternalUserId, selectSumOfScore);
            
            // WHERE externalId IS NOT NULL
            Predicate predicate = cb.isNotNull(gameAnswerRecordTable.get(DbGameAnswerRecord_.externalUserId));
            selectStatement.where(predicate);
            
            // GROUP BY externalUserId
            cb.sum(gameAnswerRecordTable.get(DbGameAnswerRecord_.score));
            selectStatement.groupBy(gameAnswerRecordTable.get(DbGameAnswerRecord_.EXTERNAL_USER_ID));
            
            // ORDER BY sum(score) desc
            Order order = cb.desc(cb.sum(gameAnswerRecordTable.get(DbGameAnswerRecord_.score)));
            selectStatement.orderBy(order);

            // Execute the query
            List<Object[]> resultList = em.createQuery(selectStatement).getResultList();
            if (resultList != null)
            {
                results = new ArrayList<>();
                for (Object[] row : resultList)
                {
                    String externalUserId = (String)row[0];
                    int totalScore = ((Number)row[1]).intValue();
                    
                    DbUserScoreInfo dbUserScoreInfo = DbUserScoreInfo.builder()
                            .externalUserId(externalUserId)
                            .totalScore(totalScore)
                            .build();
                    results.add(dbUserScoreInfo);
                }
            }
        }
        finally
        {
            if (em != null)
            {
                try
                {
                    em.close();
                }
                catch (Exception ex)
                {
                    LOGGER.info("Failed to close EntityManager.", ex);
                }
            }
        }
        
        return results == null ? Collections.emptyList() : results;
    }

    @Nonnull
    @Override
    public List<String> submitAnswers(@Nonnull String externalUserId, @Nonnull String gameId, @Nonnull List<String> answers)
    {

        List<Future<String>> futures = new ArrayList<>(answers.size());
        for (String answer : answers)
        {
            if (answer != null)
            {
                Callable<String> callable = new Callable<String>()
                {
                    @Override
                    public String call() throws Exception
                    {
                        return submitAnswer(externalUserId, gameId, answer);
                    }
                };
                
                Future<String> future = executor.submit(callable);
                futures.add(future);
            }
        }
        
        
        List<String> results = new ArrayList<String>(answers.size());
        for (Future<String> future : futures)
        {
            try
            {
                String answer = future.get();
                if (answer != null)
                {
                    // Answer was submitted!  
                    results.add(answer);
                }
            }
            catch (ExecutionException ex)
            {
                LOGGER.error("Failed to submit an answer.", ex);
            }
            catch (InterruptedException ex)
            {
                LOGGER.warn("Submit Answer thread was interrupted.", ex);
                Thread.currentThread().interrupt();
            }
        }
        
        return results;
    }
    
    
    @CheckForNull
    private String submitAnswer(@Nonnull String externalUserId, @Nonnull String gameId, @Nonnull String answer)
    {
        LOGGER.info("User " + externalUserId + " submit answer " + answer + " for game " + gameId) ;
        
        int updatedRowCount = 0;
        EntityManager em = null;
        try 
        {
            em = entityManagerFactory.createEntityManager();
            CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
            
            CriteriaUpdate<DbGameAnswerRecord> updateQuery = cb.createCriteriaUpdate(DbGameAnswerRecord.class);
            Root<DbGameAnswerRecord> tableRef = updateQuery.from(DbGameAnswerRecord.class);
            Path<Object> gameIdColumn = tableRef.get("gameId");
            Path<Object> wordColumn = tableRef.get("word");
            Path<Object> externalUserIdColumn = tableRef.get("externalUserId");
            
            Predicate whereCondition = cb.and(
                    cb.equal(gameIdColumn, gameId), 
                    cb.equal(wordColumn, answer),
                    cb.isNull(externalUserIdColumn));
            
            updateQuery.set(externalUserIdColumn, externalUserId);
            updateQuery.where(whereCondition);
            
            em.getTransaction().begin();
            updatedRowCount = em.createQuery(updateQuery).executeUpdate();
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            LOGGER.warn("Failed to submit answer.", ex);
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        
        return updatedRowCount == 0 ? null : answer;
    }

}
