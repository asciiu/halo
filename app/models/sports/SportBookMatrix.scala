package models.sports

import scala.collection.mutable

class SportBookMatrix(val sportName: String) {

  // maps a match name to the odds matrix
  // example match name: Spurs - Suns
  private val matchMatrix = mutable.Map[String, OddsMatrix2]()
  private val allBookNames = mutable.ListBuffer[String]()

  def matchNames = matchMatrix.keys.toSet
  def bookNames = allBookNames.sorted.toSet

  def matchOdds(matchName: String): List[SportsBookOdds] = {
    matchMatrix.get(matchName) match {
      case Some(matrix) => matrix.allOdds
      case None => List.empty[SportsBookOdds]
    }
  }

  def matchOddsHighA(matchName: String): Option[(String, Double)] = {
    matchMatrix.get(matchName) match {
      case Some(matrix) => matrix.highestA
      case None => None
    }
  }

  def matchOddsHighB(matchName: String): Option[(String, Double)] = {
    matchMatrix.get(matchName) match {
      case Some(matrix) => matrix.highestB
      case None => None
    }
  }

  def addMatchOdds(bookName: String, matchName: String, line1: SportsEventLine, line2: SportsEventLine): Unit = {
    allBookNames += bookName

    if (matchMatrix.contains(matchName)) {
      val odds = SportsBookOdds(bookName, line1, line2)
      matchMatrix(matchName).upsertOdds(odds)
    } else {
      val part1 = line1.name
      val part2 = line2.name
      val oddsMatrix = new OddsMatrix2(part1, part2)
      val odds = SportsBookOdds(bookName, line1, line2)
      oddsMatrix.upsertOdds(odds)
      matchMatrix += matchName -> oddsMatrix
    }
  }
}
