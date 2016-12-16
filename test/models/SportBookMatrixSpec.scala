package models

import models.sports.{SportBookMatrix, SportsBookOdds, SportsEventLine}
import org.specs2.mutable.Specification

class SportBookMatrixSpec extends Specification {

  val matrix = new SportBookMatrix("NFL Football")
  val part1 = "Cardinals"
  val part2 = "Lions"
  val bookName1 = "Nitrogen Sports"
  val matchName = s"$part1 - $part2"
  val nitroline1 = SportsEventLine(part1, 1.3)
  val nitroline2 = SportsEventLine(part2, 2.3)
  matrix.addMatchOdds(bookName1, matchName, nitroline1, nitroline2)

  val bookName2 = "Cloudbet"
  val cloudbetline1 = SportsEventLine(part1, 3.3)
  val cloudbetline2 = SportsEventLine(part2, 1.3)
  matrix.addMatchOdds(bookName2, matchName, cloudbetline1, cloudbetline2)

  val bookName3 = "Betcoin"
  val betline1 = SportsEventLine(part2, 1.1)
  val betline2 = SportsEventLine(part1, 2.0)
  matrix.addMatchOdds(bookName3, matchName, betline1, betline2)

  "A SportBookMatrix class" should {
    "have a valid sport name" in {

      matrix.sportName must beEqualTo("NFL Football")
    }
    "keep tabs on all booknames that are entered" in {
      val bookNames = matrix.bookNames
      // should be alphabetical
      bookNames must beEqualTo(Seq(bookName3, bookName2, bookName1))
    }
    "contain the correct match names" in {
      val matchNames = matrix.matchNames
      matchNames must beEqualTo(Seq(matchName))

    }
    "contain the correct odds" in {
      val matchOdds = matrix.matchOdds(matchName)
      matchOdds must beEqualTo(List(SportsBookOdds(bookName1,1.3,2.3), SportsBookOdds(bookName2,3.3,1.3), SportsBookOdds(bookName3,2.0,1.1)))
    }
    "return highest number for a" in {
      val matchOdds = matrix.matchOddsHighA(matchName)
      matchOdds must beEqualTo(Some(bookName2, 3.3))
    }
    "return highest number for b" in {
      val matchOdds = matrix.matchOddsHighB(matchName)
      matchOdds must beEqualTo(Some(bookName1, 2.3))
    }
  }
}
