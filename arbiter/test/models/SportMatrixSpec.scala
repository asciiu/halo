package models

import models.sports.{SportMatrix, SportsBookData, SportsBookOdds, SportsEvent, SportsEventOption}
import org.specs2.mutable.Specification

class SportMatrixSpec extends Specification {

  val sportname = "NBA Basketball"
  val bookname1 = "Nitrogen Sports"
  val data = SportsBookData(bookname1, sportname,
    Seq(
      SportsEvent("Denver Nuggets vs Los Angeles Clippers", "Tue, Dec 20, 2016 8:40 PM",
        Seq(
          SportsEventOption("Denver Nuggets +7",2.096),
          SportsEventOption("Denver Nuggets +7.5",1.987),
          SportsEventOption("Denver Nuggets +8",1.903),
          SportsEventOption("Los Angeles Clippers -7",1.786),
          SportsEventOption("Los Angeles Clippers -7.5",1.871),
          SportsEventOption("Los Angeles Clippers -8",1.958),
          SportsEventOption("Denver Nuggets ML",2.86),
          SportsEventOption("Los Angeles Clippers ML",1.786),
          SportsEventOption("Over 217.5",1.903),
          SportsEventOption("Over 218",1.958),
          SportsEventOption("Under 217.5",1.939),
          SportsEventOption("Under 218",1.903)
        )
      ),
      SportsEvent("Utah Jazz vs Golden State Warriors","Tue, Dec 20, 2016 8:40 PM",
        Seq(
          SportsEventOption("Over 209",1.841),
          SportsEventOption("Over 209.5",1.887),
          SportsEventOption("Under 209",1.977),
          SportsEventOption("Under 209.5",1.93)
        )
      )
    ))

  val bookname2 = "Hobo!"
  val data2 = SportsBookData(bookname2, sportname,
    Seq(
      SportsEvent("Denver Nuggets vs Los Angeles Clippers", "Tue, Dec 20, 2016 8:40 PM",
        Seq(
          SportsEventOption("Denver Nuggets +7",2.096),
          SportsEventOption("Denver Nuggets +7.5",1.987),
          SportsEventOption("Denver Nuggets +8",1.903),
          SportsEventOption("Los Angeles Clippers -7",1.786),
          SportsEventOption("Los Angeles Clippers -7.5",1.871),
          SportsEventOption("Los Angeles Clippers -8",1.958),
          SportsEventOption("Denver Nuggets ML",3.886),
          SportsEventOption("Los Angeles Clippers ML",1.283),
          SportsEventOption("Over 217.5",1.903),
          SportsEventOption("Over 218",1.958),
          SportsEventOption("Under 217.5",1.939),
          SportsEventOption("Under 218",1.903)
        )
      ),
      SportsEvent("Utah Jazz vs Golden State Warriors","Tue, Dec 20, 2016 8:40 PM",
        Seq(
          SportsEventOption("Over 209",1.841),
          SportsEventOption("Over 209.5",1.887),
          SportsEventOption("Under 209",1.977),
          SportsEventOption("Under 209.5",1.93)
        )
      )
    ))

  val bookname3 = "Cloudbet"
  val data3 = SportsBookData(bookname3, sportname,
    Seq(
      SportsEvent("Los Angeles Clippers vs Denver Nuggets", "Tue, Dec 20, 2016 8:40 PM",
        Seq(
          SportsEventOption("Denver Nuggets ML",3.806),
          SportsEventOption("Los Angeles Clippers ML",1.183),
          SportsEventOption("Over 218",1.858),
          SportsEventOption("Under 218",1.703)
        )
      ),
      SportsEvent("Utah Jazz vs Golden State Warriors","Tue, Dec 20, 2016 8:40 PM",
        Seq(
          SportsEventOption("Over 209",1.741),
          SportsEventOption("Under 209",1.777)
        )
      )
    ))

  val matrix = new SportMatrix(sportname)
  matrix.updateData(data, false)
  matrix.updateData(data2, false)
  matrix.updateData(data3, false)

  "A SportBookMatrix class" should {
    "have a valid sport name" in {

      matrix.sportName must beEqualTo(sportname)
    }
    "keep tabs on all booknames that are entered" in {
      val bookNames = matrix.bookNames
      // should be alphabetical
      bookNames must beEqualTo(Seq(bookname3, bookname2, bookname1))
    }
    "contain the correct match names" in {
      val keys = List(
        "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Denver Nuggets +7 vs Los Angeles Clippers -7",
        "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Denver Nuggets +7.5 vs Los Angeles Clippers -7.5",
        "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Denver Nuggets +8 vs Los Angeles Clippers -8",
        "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Denver Nuggets ML vs Los Angeles Clippers ML",
        "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Over 217.5 vs Under 217.5",
        "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Over 218 vs Under 218",
        "Golden State Warriors vs Utah Jazz (Tue, Dec 20, 2016) : Over 209 vs Under 209",
        "Golden State Warriors vs Utah Jazz (Tue, Dec 20, 2016) : Over 209.5 vs Under 209.5")

      matrix.keys must beEqualTo(keys)
    }
    "contain the correct odds" in {
      // test 1
      val key1 = "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Denver Nuggets +7 vs Los Angeles Clippers -7"
      val correctOdds1 = List(
        SportsBookOdds(bookname1,2.096,1.786),
        SportsBookOdds(bookname2, 2.096, 1.786)
      )
      val allOdds = matrix.allOdds(key1)
      allOdds must beEqualTo(correctOdds1)

      // test 2
      val key2 = "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Denver Nuggets ML vs Los Angeles Clippers ML"
      val correctOdds2 = List(
        SportsBookOdds(bookname1, 2.86, 1.786),
        SportsBookOdds(bookname2, 3.886, 1.283),
        SportsBookOdds(bookname3, 3.806, 1.183)
      )

      val allOdds2 = matrix.allOdds(key2)
      allOdds2 must beEqualTo(correctOdds2)
    }
    "return highest bookname and odds for option a" in {
      val key = "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Denver Nuggets ML vs Los Angeles Clippers ML"
      val matchOdds = matrix.highA(key)
      matchOdds must beEqualTo(List((bookname2, 3.886)))
    }
    "return highest bookname and odds for option b" in {
      val key = "Denver Nuggets vs Los Angeles Clippers (Tue, Dec 20, 2016) : Denver Nuggets ML vs Los Angeles Clippers ML"
      val matchOdds = matrix.highB(key)
      matchOdds must beEqualTo(List((bookname1, 1.786)))
    }
    "purge expired events when gleamed" in {
      val list = matrix.gleam("")

      list.length must beEqualTo(0)
    }
  }
}
