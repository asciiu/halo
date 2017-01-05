package arbiterJs

import com.highcharts.HighchartsUtils._
import com.highcharts.CleanJsObject
import com.highstock.config.SeriesLineData
import common.models.halo.EventData
import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import js.JSConverters._

case class Series(bookname: String, series: js.Array[SeriesLineData])

@JSExport
object ArbiterSportMatch {

  @JSExport
  def initCharts() = {
    //val matchName = jQuery("#match-name").html()
    // init the candle chart

    BookLineChart.loadData("test match").map { raw ⇒
      val str =  js.JSON.stringify(raw)
      val data = upickle.default.read[EventData](str)

      val eventTime = data.eventTime
      val eventName = data.eventName
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

      jQuery("#line-chart-a").highstock(new BookLineChart(sa))
      jQuery("#line-chart-b").highstock(new BookLineChart(sb))
    }
  }
}
