package xyz.teamnerds.wordgame.datastore;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import xyz.teamnerds.wordgame.datastore.model.DbGameAnswerRecord;
import xyz.teamnerds.wordgame.datastore.model.DbGameInfo;

public interface WordGameDatastore
{
    /**
     * Submit answers to the game datastore
     * 
     * @param userId the user submitting
     * @param gameId the game id to use
     * @param answers the list of answers
     */
    @Nonnull
    public List<String> submitAnswers(@Nonnull String userId, @Nonnull String gameId, @Nonnull List<String> answers);
    
    
    /**
     * Grab the current game in progress
     * @return
     */
    @CheckForNull
    public DbGameInfo getGameForDate(@Nonnull LocalDate date);
    
    /**
     * Get the score for the given user
     * @param externalUserId
     * @return
     */
    @Nonnull
    public DbUserScoreInfo getUserScore(@Nonnull String externalUserId);
    
    /**
     * Get all the answers that have been successfully answer by a user
     *  
     * @param gameId the game id
     * @return list of answered records
     */
    @Nonnull
    public List<DbGameAnswerRecord> getAnsweredGameRecords(@Nonnull String gameId);
    
    /**
     * Get the top users in the system
     * @return
     */
    @Nonnull
    public List<DbUserScoreInfo> getUserLeaderboards();
    

}
