package xyz.teamnerds.wordgame;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import xyz.teamnerds.wordgame.api.GameApi;
import xyz.teamnerds.wordgame.api.model.GameAnswerRecord;
import xyz.teamnerds.wordgame.api.model.GameInfo;
import xyz.teamnerds.wordgame.api.model.GameScoreInfo;
import xyz.teamnerds.wordgame.api.model.SubmitAnswerRequest;
import xyz.teamnerds.wordgame.api.model.SubmitAnswerResponse;
import xyz.teamnerds.wordgame.datastore.WordGameDatastore;
import xyz.teamnerds.wordgame.datastore.model.DbGameAnswerRecord;
import xyz.teamnerds.wordgame.datastore.model.DbGameInfo;



@Service 
public class WordGameImpl implements GameApi
{

    private static final Logger LOGGER = LoggerFactory.getLogger(WordGameImpl.class);
    
    @Autowired
    private WordGameDatastore datastore;

    @Override
    public ResponseEntity<GameScoreInfo> getLeaderboardInfo(String gameId)
    {
        List<DbGameAnswerRecord> answers = datastore.getAnsweredGameRecords(gameId);
        
        GameScoreInfo gameScoreInfo = new GameScoreInfo();
        gameScoreInfo.setGameId(gameId);
        
        for (DbGameAnswerRecord answer : answers)
        {
            GameAnswerRecord gameAnswerRecord = GsonObjectAdapter.convert(answer, GameAnswerRecord.class);
            gameScoreInfo.addAnswersItem(gameAnswerRecord);
        }
        
        return new ResponseEntity<>(gameScoreInfo, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GameInfo> getDailyGameInfo()
    {
        LocalDate now = LocalDate.now();
        DbGameInfo dbGameInfo = datastore.getGameForDate(now);
        if (dbGameInfo == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        GameInfo gameInfo = new GameInfo();
        gameInfo.setId(dbGameInfo.getGameId());
        return new ResponseEntity<>(gameInfo, HttpStatus.OK);
    }
    
    
    @Override
    public ResponseEntity<SubmitAnswerResponse> submitAnswer(String gameId, @Valid SubmitAnswerRequest submitAnswerRequest)
    {
        LOGGER.info("submitAnswer for game " + gameId + ", submitAnswerRequest=" + submitAnswerRequest);
        
        if (gameId == null || submitAnswerRequest == null)
        {
            return new ResponseEntity<SubmitAnswerResponse>(HttpStatus.BAD_REQUEST);
        }
        
        String externalUserId = submitAnswerRequest.getUser();
        if (externalUserId == null || externalUserId.length() == 0)
        {
            LOGGER.warn("Cannot submit answer without a valid external user id");
            return new ResponseEntity<SubmitAnswerResponse>(HttpStatus.BAD_REQUEST);
        }
        
        List<String> answers = submitAnswerRequest.getAnswers();
        List<String> acceptedAnswers;
        if (answers != null)
        {
            acceptedAnswers = datastore.submitAnswers(externalUserId, gameId, answers);
        }
        else
        {
            acceptedAnswers = Collections.emptyList();
        }

        SubmitAnswerResponse response = new SubmitAnswerResponse();
        response.setAcceptedAnswers(acceptedAnswers);
        return new ResponseEntity<SubmitAnswerResponse>(response, HttpStatus.OK);
    }
}
