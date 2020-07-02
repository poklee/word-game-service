package xyz.teamnerds.wordgame.datastore;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class DbUserScoreInfo
{
    private String externalUserId;
    
    private int totalScore;
}
