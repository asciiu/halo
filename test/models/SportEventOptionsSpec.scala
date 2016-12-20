package models

import java.time.LocalDateTime

import models.sports.{SportsEventLine, SportsEventPair}
import models.sports.analytics.SportEventOptions
import org.specs2.mutable.Specification

class SportEventOptionsSpec extends Specification {
  val options = new SportEventOptions("Cleveland Cavaliers - Milwaukee Bucks", LocalDateTime.now())

  val lines = Seq(
    SportsEventLine("Cleveland Cavaliers -4.5", 2.076),
    SportsEventLine("Cleveland Cavaliers -4", 1.997),
    SportsEventLine("Cleveland Cavaliers -3.5", 1.93),
    SportsEventLine("Cleveland Cavaliers -3", 1.848),
    SportsEventLine("Cleveland Cavaliers -2.5", 1.78),
    SportsEventLine("Milwaukee Bucks +4.5", 1.798),
    SportsEventLine("Milwaukee Bucks +4", 1.856),
    SportsEventLine("Milwaukee Bucks +3.5", 1.93),
    SportsEventLine("Milwaukee Bucks +3", 2.007),
    SportsEventLine("Milwaukee Bucks +2.5", 2.096),
    SportsEventLine("Cleveland Cavaliers ML", 1.607),
    SportsEventLine("Milwaukee Bucks ML", 2.412),
    SportsEventLine("Over 210", 1.792),
    SportsEventLine("Over 210.5", 1.833),
    SportsEventLine("Over 211", 1.878),
    SportsEventLine("Over 211.5", 1.93),
    SportsEventLine("Over 212", 1.967),
    SportsEventLine("Over 212.5", 2.007),
    SportsEventLine("Over 213", 2.046),
    SportsEventLine("Under 210", 2.056),
    SportsEventLine("Under 210.5", 2.007),
    SportsEventLine("Under 211", 1.967),
    SportsEventLine("Under 211.5", 1.93),
    SportsEventLine("Under 212", 1.878),
    SportsEventLine("Under 212.5", 1.833),
    SportsEventLine("Under 213", 1.792))

  options.receive(lines)
  val pairs = options.pairs()


  "A SportEentOptionsSpec class" should {
    "expire by time" in {
      options.isExpired must beEqualTo(true)
    }
    "have correct pairs" in {
      pairs.length must beEqualTo(13)
    }
    "return a correct pair" in {
      val optionA = SportsEventLine("Cleveland Cavaliers -2.5",1.78)
      val optionB = SportsEventLine("Milwaukee Bucks +2.5",2.096)

      pairs.head must beEqualTo(SportsEventPair(optionA, optionB))
    }
  }
}
