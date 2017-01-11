package arbiterJs

import arbiterJs.chart.ClientRoutes
import com.highcharts.CleanJsObject
import com.highcharts.HighchartsUtils._
import com.highstock.HighstockAliases._
import com.highstock.config._
import org.scalajs.dom.ErrorEvent
import org.scalajs.jquery.jQuery

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.ScalaJSDefined
import js.JSConverters._

/**
  * arbiterJs.BookLineChart shall arbiterJs.chart the betting odds over time
  * for bookmakers that list the same event as a betting option.
  */
@ScalaJSDefined
class BookLineChart(data: List[Series]) extends HighstockConfig {
  // remove the highcharts credits
  override val credits: Cfg[Credits] = Credits(enabled = false)
  // disable exporting
  override val exporting: Cfg[Exporting] = Exporting(enabled = false)

  // TODO this shall also be removed see below
  //override val title: Cfg[Title] = Title(
  //  text = data.eventName
  //)

  // TODO this time shall be removed because it doesn't make sense
  // to include the event time on both charts for odds a and b
  // there will be two charts now
  //override val subtitle: Cfg[Subtitle] = Subtitle(
  //  text = data.time
  //)

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

  // the range selecter shall be disabled
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

  val sdata = data.map{ s =>
    SeriesLine (
      data = s.series,
      name = s.bookname,
      tooltip = new SeriesLineTooltip {
        override val valueDecimals: UndefOr[Double] = 2
      },
      step = "left",
      lineWidth = 1
    ): CleanJsObject[SeriesLine]
  }.toArray.toJSArray.asInstanceOf[js.Array[AnySeries]]

  override val series: SeriesCfg = sdata
}

object BookLineChart {

  /**
    * Load initial arbiterJs.chart data
    * @return
    */
  def loadData(matchName: String): Future[js.Any] = {
    val promise = Promise[js.Any]()

    // pull line data for a specific event.
    val url = ClientRoutes.controllers.Arbiter.sportMatchData(matchName).url.asInstanceOf[String]

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
