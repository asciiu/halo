package models

import play.api.libs.json.Json

package object sports {

  case class SportsBookOdds(bookname: String, a: Double, b: Double)
  object SportsBookOdds {
    /**
      * Normalized item according to alpabetical order of item names
      * @return a SportBookOdds with a.name < b.name
      */
    def apply(bookname: String, line1: SportsEventOption, line2: SportsEventOption): SportsBookOdds = {

      val (a, b) = if (line1.name < line2.name) (line1.odds, line2.odds) else (line2.odds, line1.odds)
      SportsBookOdds(bookname, a, b)
    }
  }

  case class SportsEventPair(optionA: SportsEventOption, optionB: SportsEventOption)
  case class SportsEventOption(name: String, odds: Double)
  case class SportsEvent(name: String, time: String, options: Seq[SportsEventOption])
  case class SportsData(sportname: String, events: Seq[SportsEvent])
  case class SportsBookData(bookname: String, sport: String, events: Seq[SportsEvent])

  // reads
  implicit val lineRead = Json.reads[SportsEventOption]
  implicit val eventRead = Json.reads[SportsEvent]
  implicit val bookReads = Json.reads[SportsBookData]

  // writes
  //implicit val oddsWrite = Json.writes[SportsEventOdds]
  implicit val lineWrite = Json.writes[SportsEventOption]
  implicit val eventWrite = Json.writes[SportsEvent]
  implicit val bookWrites = Json.writes[SportsBookData]
}
