package models

import java.time.LocalDateTime

import models.sports.{SportsEventOption, SportsEventPair}
import models.sports.analytics.SportEventOptions
import org.specs2.mutable.Specification

class SportEventOptionsSpec extends Specification {
  val options = new SportEventOptions("Cleveland Cavaliers vs Milwaukee Bucks", LocalDateTime.now())

  val lines = Seq(
    SportsEventOption("Cleveland Cavaliers -4.5", 2.076),
    SportsEventOption("Cleveland Cavaliers -4", 1.997),
    SportsEventOption("Cleveland Cavaliers -3.5", 1.93),
    SportsEventOption("Cleveland Cavaliers -3", 1.848),
    SportsEventOption("Cleveland Cavaliers -2.5", 1.78),
    SportsEventOption("Milwaukee Bucks +4.5", 1.798),
    SportsEventOption("Milwaukee Bucks +4", 1.856),
    SportsEventOption("Milwaukee Bucks +3.5", 1.93),
    SportsEventOption("Milwaukee Bucks +3", 2.007),
    SportsEventOption("Milwaukee Bucks +2.5", 2.096),
    SportsEventOption("Cleveland Cavaliers ML", 1.607),
    SportsEventOption("Milwaukee Bucks ML", 2.412),
    SportsEventOption("Over 210", 1.792),
    SportsEventOption("Over 210.5", 1.833),
    SportsEventOption("Over 211", 1.878),
    SportsEventOption("Over 211.5", 1.93),
    SportsEventOption("Over 212", 1.967),
    SportsEventOption("Over 212.5", 2.007),
    SportsEventOption("Over 213", 2.046),
    SportsEventOption("Under 210", 2.056),
    SportsEventOption("Under 210.5", 2.007),
    SportsEventOption("Under 211", 1.967),
    SportsEventOption("Under 211.5", 1.93),
    SportsEventOption("Under 212", 1.878),
    SportsEventOption("Under 212.5", 1.833),
    SportsEventOption("Under 213", 1.792))

  options.update(lines)
  val pairs = options.pairs()


  "A SportEventOptionsSpec class" should {
    "expire by time" in {
      options.isExpired must beEqualTo(true)
    }
    "have correct pairs" in {
      pairs.length must beEqualTo(13)
    }
    "return a correct pair" in {
      val optionA = SportsEventOption("Cleveland Cavaliers -2.5",1.78)
      val optionB = SportsEventOption("Milwaukee Bucks +2.5",2.096)

      pairs.head must beEqualTo(SportsEventPair(optionA, optionB))
    }
  }
}
