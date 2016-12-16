package models.sports

import play.api.libs.json.Json

import collection.mutable

class SportsBook(val name: String) {

  // has a list of sports
  private val sports = mutable.Map[String, mutable.MutableList[SportsEvent]]()

  def importData(data: SportsBookData) = {

    // accept data only if the exchange name is the same
    if (data.exchange == name) {
      // e.g. NBA Basketball
      val sportName = data.sport
      sports.get(sportName) match {
        case Some(sportsEventList) =>
          // TODO we should update each individually
          sportsEventList.clear()
          sportsEventList ++ data.events
        case None =>
          sports += sportName -> mutable.MutableList(data.events:_*)
      }
    }
  }

  def printEvents(sportsName: String) = {
    if (sports.contains(sportsName)) {
      val sportevents = sports(sportsName)
      println(Json.prettyPrint(Json.toJson(sportevents)))
    }
  }

  // comparing sports book lines
  def event(sportsName: String, eventName: String) = {
    // each sports book event for this sport will have a
    // name and a time then a list of lines

    val sportsEvents = sports(sportsName)
    //sportsEvents.find(_.nameeventName)
  }
}
