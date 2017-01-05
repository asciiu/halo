package common.models

package object halo {

  // passed to arbiterJs client from arbiter server
  case class TimedPoint(timestamp: Long, a: Double, b: Double)
  case class BookOdds(bookname: String, odds: List[TimedPoint])
  case class EventData(eventName: String, eventTime: String, books: List[BookOdds])
}
