package models.sports


import scala.collection.mutable


class OddsMatrix2(val part1: String, val part2: String) {

  private val odds = mutable.MutableList[SportsBookOdds]()

  def akey: String = if (part1 < part2) part1 else part2
  def bkey: String = if (part1 > part2) part1 else part2
  def bookNames: Seq[String] = odds.map(_.bookname)

  def allOdds = odds.toList

  def key: String = {
    // sort alphabetically
    if (part1 < part2) s"$part1 - $part2"
    else s"$part2 - $part1"
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

    if (eventName == s"$part1 vs $part2" ||
      eventName == s"$part2 vs $part1" ||
      eventName == s"$part1 V $part2" ||
      eventName == s"$part2 V $part1" ||
      eventName == s"$part1 - $part2" ||
      eventName == s"$part2 - $part1") {
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
