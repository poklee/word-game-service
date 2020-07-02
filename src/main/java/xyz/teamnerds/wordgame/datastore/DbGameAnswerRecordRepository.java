package xyz.teamnerds.wordgame.datastore;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import xyz.teamnerds.wordgame.datastore.model.DbGameAnswerRecord;

public interface DbGameAnswerRecordRepository extends JpaRepository<DbGameAnswerRecord, LocalDate>
{
    
    public List<DbGameAnswerRecord> findAllByGameIdAndExternalUserIdIsNotNull(String gameId);


}
