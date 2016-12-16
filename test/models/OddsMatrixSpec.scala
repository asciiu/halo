package models

// external
import models.sports.SportsBookOdds
import org.specs2.mutable._

// internal
import models.sports.OddsMatrix2
import models.sports.{SportsEventLine}


class OddsMatrixSpec extends Specification {

  "An OddsMatrix2 class" should {
    "alphabetically sort the participants names in toString" in {

      val part1 = "World"
      val part2 = "Miami Heat"
      val gameOddsMatrix = new OddsMatrix2(part1, part2)

      gameOddsMatrix.toString() must beEqualTo(s"$part2 - $part1")
    }
    "recognize an event name in different formats'" in {

      val part1 = "Monkeys"
      val part2 = "Tigers"

      val gameOddsMatrix = new OddsMatrix2(part1, part2)

      // should match vs and -
      val isMatch1 = gameOddsMatrix.isEvent(s"$part2 vs $part1")
      val isMatch2 = gameOddsMatrix.isEvent(s"$part1 - $part2")

      isMatch1 ==== true
      isMatch2 ==== true
    }
    "must return the correct keys" in {

      val part1 = "Monkeys"
      val part2 = "Tigers"

      val gameOddsMatrix1 = new OddsMatrix2(part1, part2)
      val gameOddsMatrix2 = new OddsMatrix2(part2, part1)

      gameOddsMatrix1.key must beEqualTo("Monkeys - Tigers")
      gameOddsMatrix1.akey must beEqualTo(part1)
      gameOddsMatrix1.bkey must beEqualTo(part2)

      gameOddsMatrix2.key must beEqualTo("Monkeys - Tigers")
      gameOddsMatrix1.akey must beEqualTo(part1)
      gameOddsMatrix1.bkey must beEqualTo(part2)
    }
    "must return None for highest values when matrix is empty" in {
      val part1 = "Monkeys"
      val part2 = "Tigers"

      val gameOddsMatrix1 = new OddsMatrix2(part1, part2)

      gameOddsMatrix1.highestA must be(None)
      gameOddsMatrix1.highestB must be(None)
    }
    "must be valid when inserting odds" in {
      val part1 = "Monkeys"
      val part2 = "Tigers"
      val book1 = "book1"
      val book2 = "book2"

      val matrix = new OddsMatrix2(part1, part2)

      val line1 = SportsEventLine(part1, 1.3)
      val line2 = SportsEventLine(part2, 4.6)
      val odds1 = SportsBookOdds(book1, line1, line2)
      matrix.upsertOdds(odds1)

      val line3 = SportsEventLine(part2, 3.8)
      val line4 = SportsEventLine(part1, 1.2)
      val odds2 = SportsBookOdds(book2, line3, line4)
      matrix.upsertOdds(odds2)

      val line5 = SportsEventLine(part2, 3.3)
      val line6 = SportsEventLine(part1, 4.2)
      val odds3 = SportsBookOdds(book2, line5, line6)
      matrix.upsertOdds(odds3)

      matrix.highestA must beEqualTo(Some(book2, 4.2))
      matrix.highestB must beEqualTo(Some(book1, 4.6))

      // there should only be 2 odds
      // 1 for each book
      matrix.allOdds.length must beEqualTo(2)
    }
  }
}