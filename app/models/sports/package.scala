package models

package object sports {

  case class SportsEventOdds(europe: Double, american: Option[Int])
  case class SportsEventLine(name: String, odds: SportsEventOdds)
  case class SportsEvent(name: String, time: String, lines: Seq[SportsEventLine])
  case class SportsBook(exchange: String, sport: String, events: Seq[SportsEvent])

}
