package models.sports.analytics

import common.models.halo.TimedPoint
import java.time.{LocalDateTime, ZoneOffset}
import scala.collection.mutable

/**
  * Keeps track of the movement on odds for a given option name.
  * @param optionName
  * @param openA
  * @param openB
  * @param opened timestamp of open time as Long seconds
  */
class OddsTracker(val optionName: String, val openA: Double, val openB: Double, val opened: Long) {

  private val odds = mutable.ListBuffer[TimedPoint]()
  odds.append(TimedPoint(opened, openA, openB))

  def currentOdds = odds.last
  def currentA = odds.last.a
  def currentB = odds.last.b

  def allOdds = odds.toList

  def trackMovement(a: Double, b: Double): Boolean = {
    val flag = if (currentA != a || currentB != b) true else false

    val time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    odds.append(TimedPoint(time, a, b))
    flag
  }
}
