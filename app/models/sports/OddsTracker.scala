package models.sports

import java.time.LocalDateTime
import scala.collection.mutable


class OddsTracker(val optionName: String, val open: Double) {

  case class Stamp(odds: Double, timestamp: LocalDateTime)

  val odds = mutable.ListBuffer[Stamp]()

  def trackMovement(o: Double): Boolean = {
    if (odds.nonEmpty) {
      val lastOdds = odds.last
      if (lastOdds.odds != o) {
        println(s"$optionName ${lastOdds.odds} $o")
        odds.append(Stamp(o, LocalDateTime.now()))
        true
      } else {
        false
      }
    } else {
      odds.append(Stamp(o, LocalDateTime.now()))
      true
    }
  }
}
