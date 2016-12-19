package models.sports

import java.time.LocalDateTime
import scala.collection.mutable


class OddsTracker(val optionName: String, val open: Double) {

  case class Stamp(odds: Double, timestamp: LocalDateTime)

  val odds = mutable.ListBuffer[Stamp]()
  odds.append(Stamp(open, LocalDateTime.now()))

  def currentOdds = odds.last.odds

  def trackMovement(o: Double): Boolean = {
    val lastOdds = odds.last
    if (lastOdds.odds != o) {
      println(s"$optionName ${lastOdds.odds} to $o")
      odds.append(Stamp(o, LocalDateTime.now()))
      true
    } else {
      false
    }
  }
}
