package models.sports


import scala.collection.mutable


/**
  * Keeps track of odds between a single pair (optionA vs optionB)
  * across different bookmakers
  *
  * @param optionA the first option
  * @param optionB the second option
  */
class OddsMatrixAB(val optionA: String, val optionB: String) {

  private val odds = mutable.MutableList[SportsBookOdds]()

  // always return the lesser of the options
  def akey: String = if (optionA < optionB) optionA else optionB
  // always return the greater of the options
  def bkey: String = if (optionA > optionB) optionA else optionB

  def bookNames: Seq[String] = odds.map(_.bookname)

  def allOdds = odds.toList

  def key: String = {
    // sort alphabetically
    if (optionA < optionB) s"$optionA - $optionB"
    else s"$optionB - $optionA"
  }

  def highestA: Option[(String, Double)] = {
    if (odds.nonEmpty) {
      val high = odds.sortBy(_.a).reverse.head
      Some((high.bookname, high.a))
    } else None
  }

  def highestB: Option[(String, Double)] = {
    if (odds.nonEmpty) {
      val high = odds.sortBy(_.b).reverse.head
      Some((high.bookname, high.b))
    } else None
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

  def upsertOdds(o: SportsBookOdds): Unit = {
    var bookname = o.bookname
    val index = odds.indexWhere(_.bookname == bookname)
    // if previous odds present update it
    if (index != -1) odds.update(index, o)
    else odds += o
  }
}
