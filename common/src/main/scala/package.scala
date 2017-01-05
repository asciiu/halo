package common.models

package object halo {

  case class BookPoint(timestamp: Long, a: Double, b: Double)
  case class BookOdds(bookname: String, points: List[BookPoint])
  case class EventData(eventName: String, odds: List[BookOdds])
}
