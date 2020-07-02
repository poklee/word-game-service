package xyz.teamnerds.wordgame.datastore;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

import xyz.teamnerds.wordgame.datastore.model.DbDailyGame;

public interface DbDailyGameRepository extends CrudRepository<DbDailyGame, LocalDate>
{


}
