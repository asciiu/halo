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

  private var movementCounter = 0
  private val odds = mutable.ListBuffer[TimedPoint]()
  odds.append(TimedPoint(opened, openA, openB))

  def currentOdds: TimedPoint = odds.last
  def currentA: Double = odds.last.a
  def currentB: Double = odds.last.b

  // sorted by timestamp
  def allOdds: List[TimedPoint] = odds.toList

  def movementCount = movementCounter

  def trackMovement(a: Double, b: Double): Boolean = {
    val flag = if (currentA != a || currentB != b) {
      movementCounter += 1
      true
    } else {
      false
    }

    val time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    odds.append(TimedPoint(time, a, b))
    flag
  }
}
