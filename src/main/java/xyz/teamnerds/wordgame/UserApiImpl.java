package xyz.teamnerds.wordgame;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import xyz.teamnerds.wordgame.api.UserApi;
import xyz.teamnerds.wordgame.api.model.UserLeaderboardInfo;
import xyz.teamnerds.wordgame.api.model.UserScoreInfo;
import xyz.teamnerds.wordgame.datastore.DbUserScoreInfo;
import xyz.teamnerds.wordgame.datastore.WordGameDatastore;


@Service 
public class UserApiImpl implements UserApi
{
    
    @Autowired
    private WordGameDatastore datastore;

    @Override
    public ResponseEntity<UserScoreInfo> getUserScore(String externalUserId)
    {
        DbUserScoreInfo dbUserScoreInfo = datastore.getUserScore(externalUserId);

        UserScoreInfo userScoreInfo = new UserScoreInfo();
        userScoreInfo.setExternalUserId(externalUserId);
        userScoreInfo.setTotalScore(dbUserScoreInfo.getTotalScore());
        return new ResponseEntity<>(userScoreInfo, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<UserLeaderboardInfo> getUserLeaderboards()
    {
        List<DbUserScoreInfo> userScores = datastore.getUserLeaderboards();
        List<UserScoreInfo> userScoreInfos = userScores.stream()
                .map(this::toUserScoreInfo)
                .collect(Collectors.toList());
        
        UserLeaderboardInfo userLeaderboardInfo = new UserLeaderboardInfo();
        userLeaderboardInfo.setTopUsers(userScoreInfos);
        return new ResponseEntity<>(userLeaderboardInfo, HttpStatus.OK);
    }
    
    private UserScoreInfo toUserScoreInfo(DbUserScoreInfo dbObject)
    {
        UserScoreInfo obj = new UserScoreInfo();
        obj.setExternalUserId(dbObject.getExternalUserId());
        obj.setTotalScore(dbObject.getTotalScore());
        return obj;
    }

}
