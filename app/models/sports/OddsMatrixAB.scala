package models.sports


import java.time.LocalDateTime

import scala.collection.mutable


/**
  * Keeps track of odds between a single pair (optionA vs optionB)
  * across different bookmakers
  *
  * @param optionA the first option
  * @param optionB the second option
  */
class OddsMatrixAB(val expiration: LocalDateTime, val optionA: String, val optionB: String) {

  private val odds = mutable.MutableList[SportsBookOdds]()

  private var isArb = false

  def isExpired: Boolean = LocalDateTime.now().isAfter(expiration)

  // always return the lesser of the options
  def akey: String = if (optionA < optionB) optionA else optionB
  // always return the greater of the options
  def bkey: String = if (optionA > optionB) optionA else optionB

  def bookNames: Seq[String] = odds.map(_.bookname).sorted

  def allOdds = odds.toList

  def allOddsA = odds.sortBy(_.bookname).map(_.a)
  def allOddsB = odds.sortBy(_.bookname).map(_.b)

  def key: String = {
    // sort alphabetically
    if (optionA < optionB) s"$optionA vs $optionB"
    else s"$optionB vs $optionA"
  }

  def highestA: List[(String, Double)] = {
    if (odds.nonEmpty) {
      val sorted = odds.sortBy(_.a).reverse
      val high = sorted.head
      val allHighs = sorted.takeWhile(_.a == high.a)
      allHighs.map( x => (x.bookname, x.a)).sortBy(_._1).toList
    } else List[(String, Double)]()
  }

  def highestB: List[(String, Double)] = {
    if (odds.nonEmpty) {
      val sorted = odds.sortBy(_.b).reverse
      val high = sorted.head
      val allHighs = sorted.takeWhile(_.b == high.b)
      allHighs.map( x => (x.bookname, x.b)).sortBy(_._1).toList
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
    if (isArb == true) {
      val higha = highestA.head
      val highb = highestB.head
      val total = 1 / higha._2 + 1 / highb._2

      println("")
      println(s"${LocalDateTime.now()}")
      println(eventName)
      println(s"$optionA vs $optionB")
      println(s"$optionA: ${higha._1} - ${higha._2}")
      println(s"$optionB: ${highb._1} - ${highb._2}")
      println(s"total: ${total}")
    }
  }

  def upsertOdds(o: SportsBookOdds): Unit = {
    var bookname = o.bookname
    val index = odds.indexWhere(_.bookname == bookname)
    // if previous odds present update it
    if (index != -1) odds.update(index, o)
    else odds += o

    // arb?
    val higha = highestA.head
    val highb = highestB.head
    val total = 1 / higha._2 + 1 / highb._2
    if (total < 1.0) isArb = true
    else isArb = false
  }
}
