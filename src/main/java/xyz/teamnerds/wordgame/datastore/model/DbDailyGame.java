package xyz.teamnerds.wordgame.datastore.model;

import java.time.LocalDate;

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
@Table(name = "DailyGame")
public class DbDailyGame
{
    
    @Column(name = "game_date")
    @Id
    private LocalDate gameDate;
    
    @Column(name = "game_id")
    private String gameId;

}
