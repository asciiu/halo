import chart.ClientRoutes
import com.highcharts.CleanJsObject
import com.highcharts.HighchartsUtils._
import com.highstock.HighstockAliases._
import com.highstock.config._
import common.models.halo.EventData
import org.scalajs.dom.ErrorEvent
import org.scalajs.jquery.jQuery

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * BookLineChart shall chart the betting odds over time
  * for bookmakers that list the same event as a betting option.
  */
@ScalaJSDefined
class BookLineChart(data: EventData) extends HighstockConfig {
  // remove the highcharts credits
  override val credits: Cfg[Credits] = Credits(enabled = false)
  // disable exporting
  override val exporting: Cfg[Exporting] = Exporting(enabled = false)

  override val title: Cfg[Title] = Title(
    text = data.eventName
  )

  override val subtitle: Cfg[Subtitle] = Subtitle(
    text = "1 Jan, 2016 4:30pm"
  )

  override val chart: Cfg[Chart] = Chart(
    zoomType = "x"
  )

  override val xAxis: Cfg[XAxis] = XAxis(
    gridLineWidth = 0
  )
  override val scrollbar: Cfg[Scrollbar] = Scrollbar(
    enabled = false
  )
  override val navigator: Cfg[Navigator] = Navigator(
    enabled = false
  )

  override val yAxis: Cfg[YAxis] = YAxis(
    gridLineWidth = 0,
    title = YAxisTitle(text = "flowbets.com"),
    minorGridLineWidth = 0,
    opposite = false
  )

  override val plotOptions: Cfg[PlotOptions] = PlotOptions(
    area = PlotOptionsArea(
      lineWidth = 1
    )
  )

  override val rangeSelector: Cfg[RangeSelector] = new RangeSelector {
    override val buttons: UndefOr[js.Array[js.Any]] = js.Array(
      js.Dynamic.literal(`type` = "hour", count = 1, text = "1H"),
      js.Dynamic.literal(`type` = "day", count = 1, text = "1D"),
      js.Dynamic.literal(`type` = "all", count = 1, text = "All")
    )
    override val selected: UndefOr[Double] = 1
    override val inputEnabled: UndefOr[Boolean] = false
    override val enabled: UndefOr[Boolean] = false
  }

  import js.JSConverters._
  val bookname = data.odds.map(x => (x.bookname, x.points))
  val seriesD1 = bookname.head._2.map{ p =>
    SeriesLineData(
      x = p.timestamp.asInstanceOf[Double],
      y = p.a) : CleanJsObject[SeriesLineData]
  }.toArray.toJSArray

  override val series: SeriesCfg = js.Array[AnySeries](
    SeriesLine (
      data = seriesD1,
      name = bookname.head._1,
      tooltip = new SeriesLineTooltip {
        override val valueDecimals: UndefOr[Double] = 2
      },
      step = "left",
      lineWidth = 1
    ),
    SeriesLine (
      data = js.Array[js.Array[js.Any]](),
      name = bookname.head._1,
      tooltip = new SeriesLineTooltip {
        override val valueDecimals: UndefOr[Double] = 2
      },
      step = "left",
      lineWidth = 1
    )
  )
}

object BookLineChart {
  def loadSampleData(): Future[js.Any] = {
    val promise = Promise[js.Any]()

    // pull line data for a specific event.
    val url = ClientRoutes.controllers.Arbiter.sportMatchData("test").url.asInstanceOf[String]

    //val socket = new WebSocket(s"ws://localhost:9000$url")
    val xhr = jQuery.getJSON(s"http://localhost:9000$url", (data: js.Any) ⇒ {
      promise.trySuccess(data)
    })
    xhr.onerror = { e: ErrorEvent ⇒
      promise.tryFailure(new Exception(e.message))
    }
    promise.future
  }
}
