package models.sports

// External
import java.time.{LocalDateTime, OffsetDateTime}
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import com.sun.scenario.effect.Offset
import models.db.Tables.TranslationsRow

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// Internal
import models.db.Tables
import services.DBService
import utils.db.TetraoPostgresDriver.api._


class Normalizer @Inject() (val database: DBService) {

  def process(data: SportsBookData): Future[SportsBookData] = {
    val correctedTimes = correctEventTime(data)

    correctSportName(correctedTimes).flatMap { correctedSportName =>
      correctEventName(correctedSportName)
    }
  }

  def correctSportName(data: SportsBookData): Future[SportsBookData] = {
    // we shall normalize the sport names from the books because these sport names
    // may be labeled differently on various bookmakers sites
    val sportname = data.sport

    val query = Tables.Translations.filter { row =>
      row.context === "Sport Name" && row.wording === sportname
    }

    database.runAsync(query.result.headOption).map { opt =>
      opt match {
        case Some(row) => data.copy(sport = row.normalization)
        case _ => data
      }
    }
  }

  def correctEventName(data: SportsBookData): Future[SportsBookData] = {

    val teamNames = mutable.ListBuffer[String]()
    for (event <- data.events) {
      val name = event.name.replace(" Vs ", " vs ")
      val teams = name.split(" vs ")
      teamNames.append(teams: _*)
    }

    val query = Tables.Translations.filter { rows =>
      rows.context === data.sport && rows.wording.inSet(teamNames)
    }

    val defaultTranslation = TranslationsRow(0, "", "", "", OffsetDateTime.now(), OffsetDateTime.now())

    database.runAsync(query.result).map { rows =>
      val newEvents = mutable.ListBuffer[SportsEvent]()

      for (event <- data.events) {
        // all event names should be in proper case
        // uppercase first letter lower case all else
        val name = event.name.replace(" Vs ", " vs ")
        val teams = name.split(" vs ")

        val team1 = rows.find(_.wording == teams(0))
          .getOrElse(defaultTranslation.copy(normalization = teams(0))).normalization

        val team2 = rows.find(_.wording == teams(1))
          .getOrElse(defaultTranslation.copy(normalization = teams(1))).normalization

        val normalized = name.replace(teams(0), team1).replace(teams(1), team2)

        val options = mutable.ListBuffer[SportsEventOption]()
        for (option <- event.options) {
          val optionName = option.name
            .replace(" Ml", " ML")
            .replace(teams(0), team1)
            .replace(teams(1), team2)

          options.append(option.copy(name = optionName))
        }

        newEvents.append(event.copy(name = normalized, options = options))
      }

      data.copy(events = newEvents)
    }
  }

  /**
    * Normalize the event time so if fits nicely on a 15 minute
    * boundary. This is required because some bookmakers report
    * event time as 5:05pm instead of 5:00pm.
    * @param data
    * @return
    */
  def correctEventTime(data: SportsBookData): SportsBookData = {
    val newEvents = mutable.ListBuffer[SportsEvent]()
    for (event <- data.events) {

      // all event names should be in proper case
      // uppercase first letter lower case all else
      val time = event.time
      val formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy h:mm a")
      val newTime = LocalDateTime.parse(time, formatter)
      val unroundedMinutes = newTime.getMinute()
      val mod = unroundedMinutes % 30
      val delta = if (mod < 16) -mod else (30-mod)
      val rounded = newTime.plusMinutes(delta)

      newEvents.append(event.copy(time = rounded.format(formatter)))
    }

    data.copy(events = newEvents)
  }
}
