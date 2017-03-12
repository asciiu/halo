package models.sports

import common.models.halo.{BookOdds, EventData}
import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}
import models.sports.analytics.OddsTracker
import scala.collection.mutable

/**
  * Keeps track of odds between a single pair (optionA vs optionB)
  * across different bookmakers
  *
  * @param optionA the first option
  * @param optionB the second option
  */
class OddsMatrixAB(val eventID: String, val expiration: LocalDateTime, val optionA: String, val optionB: String) {

  // Maps bookname to the book odds
  private val odds = mutable.Map[String, OddsTracker]()

  def isExpired: Boolean = LocalDateTime.now().isAfter(expiration)

  // always return the lesser of the options
  def akey: String = if (optionA < optionB) optionA else optionB
  // always return the greater of the options
  def bkey: String = if (optionA > optionB) optionA else optionB

  def bookNames: Seq[String] = odds.keys.toSeq

  def hasShifted(count: Int): Boolean = {
    odds.find( _._2.movementCount > count) match {
      case Some(_) => true
      case None => false
    }
  }

  /**
    * All the latest odds
    * @return list of SportBookOdds
    */
  def allOdds: List[SportsBookOdds] = {
    odds.map {
      case (bookname, tracker) =>
        SportsBookOdds(bookname, tracker.currentA, tracker.currentB)
    }.toList
  }

  /**
    * @return EventData object
    */
  def allOddsHistory: EventData = {
    val allOdds = odds.map {
      case (bookname, tracker) =>
        BookOdds(bookname, tracker.allOdds)
    }.toList

    EventData(key, expiration.toString, allOdds)
  }

  def allOddsA = allOdds.map(_.a)
  def allOddsB = allOdds.map(_.b)

  def key: String = {
    // sort alphabetically
    if (optionA < optionB) s"$optionA vs $optionB"
    else s"$optionB vs $optionA"
  }

  def highestA: List[(String, Double)] = {
    if (odds.nonEmpty) {
      val sorted = allOdds.sortBy(_.a).reverse
      val high = sorted.head
      val allHighs = sorted.takeWhile(_.a == high.a)
      allHighs.map( x => (x.bookname, x.a)).sortBy(_._1)
    } else List[(String, Double)]()
  }

  def highestB: List[(String, Double)] = {
    if (odds.nonEmpty) {
      val sorted = allOdds.sortBy(_.b).reverse
      val high = sorted.head
      val allHighs = sorted.takeWhile(_.b == high.b)
      allHighs.map( x => (x.bookname, x.b)).sortBy(_._1)
    } else List[(String, Double)]()
  }

  override def toString() = key

  /**
    * The event name is permitted to be recognized
    * in several different formats - vs, VS, -
    * @param eventName
    * @return true if the participants are the same
    */
  def isEvent(eventName: String): Boolean = {

    if (eventName == s"$optionA vs $optionB" ||
      eventName == s"$optionB vs $optionA" ||
      eventName == s"$optionA V $optionB" ||
      eventName == s"$optionB V $optionA" ||
      eventName == s"$optionA - $optionB" ||
      eventName == s"$optionB - $optionA") {
      true
    } else {
      false
    }
  }

  def displayArb(eventName: String) = {

    // compare the latest odds for A
    // with each highest value for B in each snapshot

    // compare the latest odds for B
    // with each highest value for A in each snapshot

    //if (odds.nonEmpty) {
    //  val allBookOdds = odds.map { case (bookname, tracker) =>
    //    SportsBookOdds(
    //      bookname,
    //      tracker.currentA,
    //      tracker.currentB)
    //    }.toList

    //    // arb?
    //    val higha = allBookOdds.sortBy(_.a).last
    //    val highb = allBookOdds.sortBy(_.b).last
    //    val total = 1 / higha.a + 1 / highb.b

    //    if (total < 1.0) {
    //    }
    //  }

//      val higha = highestA.head
//      val highb = highestB.head
//      val total = 1 / higha._2 + 1 / highb._2
//
//      println("")
//      println(s"${LocalDateTime.now()}")
//      println(eventName)
//      println(s"$optionA vs $optionB")
//      println(s"$optionA: ${higha._1} - ${higha._2}")
//      println(s"$optionB: ${highb._1} - ${highb._2}")
//      println(s"total: ${total}")
  }

  def upsertOdds(o: SportsBookOdds): Unit = {
    // if there are no odds trackers for this bookname
    val tracker = odds.get(o.bookname) match {
      case Some(tracker) =>
        tracker.trackMovement(o.a, o.b)
        tracker
      case None =>
        val time = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()

        val newTracker = new OddsTracker(key, o.a, o.b, time)
        odds += o.bookname -> newTracker
        newTracker
    }
  }

  def snapShots: Unit = {
    //odds.map { case (bookname, tracker) =>
    //  SportsBookSnapShots(bookname, tracker.allOdds)
    //}.toList
    val allBookOdds = odds.map { case (bookname, tracker) =>
      val odds = tracker.currentOdds
      SportsBookTimedOdds(bookname, odds.timestamp, odds.a, odds.b)
    }.toList

    // arb?
    val higha = allBookOdds.sortBy(_.a).last
    val highb = allBookOdds.sortBy(_.b).last

    //val total = 1 / higha.a + 1 / highb.b
    //if (total < 1.0) true
    //else false
  }
}
