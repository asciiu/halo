package arbiterJs

import arbiterJs.chart.ClientRoutes
import com.highcharts.HighchartsUtils._
import com.highcharts.CleanJsObject
import com.highstock.config.SeriesLineData
import common.models.halo.EventData
import org.scalajs.dom.ErrorEvent
import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import js.JSConverters._
import scala.concurrent.{Future, Promise}
import scala.scalajs.js.JSON

case class Series(bookname: String, series: js.Array[SeriesLineData])

@JSExport
object SportingEvent {

  @JSExport
  def initCharts(eventID: Int) = {
    //val matchName = jQuery("#match-name").html()
    // init the candle chart

    loadData(eventID).map { raw ⇒
      val str =  js.JSON.stringify(raw)
      val data = upickle.default.read[EventData](str)

      val eventTime = data.eventTime
      val eventName = data.eventName.split(" vs ")
      val seriesData = data.books.map { book =>
        val bookname = book.bookname
        val bookOdds = book.odds

        val seriesLineDataA = bookOdds.map { odds =>
          SeriesLineData(
            x = odds.timestamp.asInstanceOf[Double],
            y = odds.a
          )
        }.toArray.toJSArray

        val seriesLineDataB = bookOdds.map { odds =>
          SeriesLineData(
            x = odds.timestamp.asInstanceOf[Double],
            y = odds.b
          )
        }.toArray.toJSArray

        val seriesA = Series(bookname, seriesLineDataA)
        val seriesB = Series(bookname, seriesLineDataB)

        (seriesA, seriesB)
      }

      val sa = seriesData.map(_._1)
      val sb = seriesData.map(_._2)

      jQuery("#line-chart-a").highstock(new BookLineChart(eventName.head, sa))
      jQuery("#line-chart-b").highstock(new BookLineChart(eventName.last, sb))
    }
  }

  /**
    * Load initial chart data for sporting event from server.
    */
  private def loadData(eventID: Int): Future[js.Any] = {
    val promise = Promise[js.Any]()

    // pull line data for a specific event.
    val url = ClientRoutes.controllers.Arbiter.sportEventOdds(eventID).url.asInstanceOf[String]

    // TODO pull host from config
    val xhr = jQuery.getJSON(s"http://localhost:9000$url", (data: js.Any) ⇒ {
      promise.trySuccess(data)
    })
    xhr.onerror = { e: ErrorEvent ⇒
      promise.tryFailure(new Exception(e.message))
    }
    promise.future
  }
}
