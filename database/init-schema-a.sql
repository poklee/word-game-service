-- Name of this file is important, mysql init files base on filename, the expected order is
-- init-schema-a.sql
-- init-schema-b.sql 

DROP TABLE IF EXISTS GameInfo;
DROP TABLE IF EXISTS DailyGame;
DROP TABLE IF EXISTS GameAnswerRecord;

CREATE TABLE `GameInfo` (
  `game_id` varchar(32) NOT NULL,
  PRIMARY KEY (`game_id`)
) ENGINE=InnoDB;

CREATE TABLE `DailyGame` (
  `game_date` DATE NOT NULL,
  `game_id` varchar(32) NOT NULL,
  PRIMARY KEY(`game_date`)
);

CREATE TABLE `GameAnswerRecord` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `game_id` varchar(32) NOT NULL,
  `word` varchar(32) NOT NULL,
  `score` INT NOT NULL,
  `external_user_id` varchar(255) DEFAULT NULL,
  `last_updated` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE(`game_id`, `word`),  
  KEY `GameAnswerRecord_external_user_id_index` (`external_user_id`)
) ENGINE=InnoDB;
