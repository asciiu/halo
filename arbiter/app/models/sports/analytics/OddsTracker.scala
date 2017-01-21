package models.sports.analytics

import java.time.{LocalDateTime, ZoneOffset}

import scala.collection.mutable

/**
  * Keeps track of the movement on odds for a given option name.
  * @param optionName
  * @param open
  * @param opened timestamp of open time as Long seconds
  */
class OddsTracker(val optionName: String, val open: Double, val opened: Long) {

  case class Stamp(odds: Double, timestamp: Long)

  val odds = mutable.ListBuffer[Stamp]()
  odds.append(Stamp(open, opened))

  def currentOdds = odds.last.odds

  def trackMovement(o: Double): Boolean = {
    val lastOdds = odds.last
    val flag = if (lastOdds.odds != o) true else false

    odds.append(Stamp(o, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)))
    flag
  }
}
