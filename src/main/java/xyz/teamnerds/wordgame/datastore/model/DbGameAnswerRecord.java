package xyz.teamnerds.wordgame.datastore.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="GameAnswerRecord")
public class DbGameAnswerRecord
{
    @Id
    @Column(name="id")
    private int id;
    
    @Column(name="game_id")
    private String gameId;
    
    @Column(name="word")
    private String word;
    
    @Column(name="score")
    private int score;
    
    @Column(name="external_user_id")
    private String externalUserId;
    
    @Column(name="last_updated")
    private ZonedDateTime lastUpdated;
}
