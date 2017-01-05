package chart

import com.highcharts.CleanJsObject
import com.highcharts.HighchartsUtils._
import com.highstock.HighstockAliases._
import com.highstock.config._
import org.scalajs.dom.raw.ErrorEvent
import org.scalajs.jquery.jQuery

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.js.{Object, UndefOr}


@js.native
@js.annotation.JSName("jsRoutes")
object ClientRoutes extends js.Object {
  def controllers:js.Dynamic = js.native
}

object CandleChartConfig {
  def loadData(marketName: String): Future[js.Array[js.Array[Double]]] = {
    val route = ClientRoutes.controllers.PoloniexController.candles(marketName)

    val promise = Promise[js.Array[js.Array[Double]]]()
    val xhr = jQuery.getJSON(route.url.toString, (data: js.Array[js.Array[Double]]) ⇒ {
      promise.trySuccess(data)
    })
    xhr.onerror = { e: ErrorEvent ⇒
      promise.tryFailure(new Exception(e.message))
    }
    promise.future
  }
}

/**
  *
  */
@ScalaJSDefined
class CandleChart(marketName: String, ledata: js.Array[js.Array[js.Any]]) extends HighstockConfig {

  var plotRight: Double = 0

  def tooltipPositioner = { (labelWidth: Double, labelHeight: Double, point: Object) =>
    //val chart = api.plotWidth - labelWidth + api.plotLeft,
    js.Dynamic.literal(
      x = plotRight - labelWidth,
      y = 0
    )
  }: js.Function3[Double, Double, Object, Object]

  def tooltipFormatter = { (thiz:js.Dynamic) =>
    val x = thiz.x
    val points = thiz.points.asInstanceOf[js.Array[Dynamic]]
    if (points.length == 3) {
      val point = points.apply(0).asInstanceOf[js.Dynamic].point
      val open = point.open.toFixed(8)
      val high = point.high.toFixed(8)
      val low = point.low.toFixed(8)
      val close = point.close.toFixed(8)
      val date = new js.Date(x.asInstanceOf[Double])
      s"<b>$date O:</b>$open <b>H:</b>$high<b> L:</b>$low<b> C: </b>$close"
    } else {
      ""
    }
  }:js.ThisFunction0[js.Dynamic, String]

  // remove the highcharts credits
  override val credits: Cfg[Credits] = Credits(enabled = false)
  // disable exporting
  override val exporting: Cfg[Exporting] = Exporting(enabled = false)

  override val tooltip: Cfg[Tooltip] = Tooltip(
    style = js.Dynamic.literal(
      fontSize = "8pt"
    ),
    enabled = true,
    shadow = false,
    borderWidth = 0,
    shared = true,
    backgroundColor = "rgba(0, 0, 0, 0.0)",
    formatter = tooltipFormatter,
    positioner = tooltipPositioner
  )

  // TODO all styles should be in the style sheets
  //override val subtitle: Cfg[Subtitle] = Subtitle(
  //  text = marketName,
  //  style = js.Dynamic.literal(
  //    fontSize = "10px"
  //  )
  //)
  override val title: Cfg[Title] = Title(
    text = marketName,
    align = "left",
    style = js.Dynamic.literal(
      fontSize = "14px"
    )
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
    minorGridLineWidth = 0,
    opposite = false,
    labels = YAxisLabels(
      format = "{value:.8f}"
    ),
    crosshair = YAxisCrosshair(
      snap = false,
      label = YAxisCrosshairLabel(
        enabled = true,
        backgroundColor = "rgba(85, 162, 214, 0.9)",
        format = "{value:.8f}"
      )
    ): CleanJsObject[YAxisCrosshair]
  )

  override val plotOptions: Cfg[PlotOptions] = PlotOptions(
    area = PlotOptionsArea(
      lineWidth = 1
    ),
    candlestick = PlotOptionsCandlestick(
      cropThreshold = 600
    )
  )

  override val rangeSelector: Cfg[RangeSelector] = new RangeSelector {
    override val buttons: UndefOr[js.Array[js.Any]] = js.Array(
      js.Dynamic.literal(`type` = "hour", count = 1, text = "1H"),
      js.Dynamic.literal(`type` = "hour", count = 7, text = "7H"),
      js.Dynamic.literal(`type` = "day", count = 1, text = "1D"),
      js.Dynamic.literal(`type` = "all", count = 1, text = "All")
    )
    override val selected: UndefOr[Double] = 3
    override val inputEnabled: UndefOr[Boolean] = false
    override val enabled: UndefOr[Boolean] = false
    //override val buttonTheme: UndefOr[Object] = js.Dynamic.literal(visibility = "hidden")
    //override val labelStyle: UndefOr[Object] = js.Dynamic.literal(visibility = "hidden")
  }

  println (ledata)

  val lecand = ledata.map { dat =>
    SeriesCandlestickData (
      //override val name = dat(0).asInstanceOf[UndefOr[String]]
      x = dat(0).asInstanceOf[Double],
      open = dat(1).asInstanceOf[Double],
      high = dat(2).asInstanceOf[Double],
      low = dat(3).asInstanceOf[Double],
      close = dat(4).asInstanceOf[Double]
    )
  }

  override val series: SeriesCfg = js.Array[AnySeries](
    SeriesCandlestick (
      name = "Candles",
      allowPointSelect = true,
      tooltip = new SeriesCandlestickTooltip {
        override val valueDecimals: UndefOr[Double] = 3
      },
      color = "rgba(255, 102, 102, 1.0)",
      lineColor = "rgba(255, 102, 102, 1)",
      upColor = "rgba(112, 219, 112, 1.0)",
      upLineColor = "rgba(112, 219, 112, 1.0)",
      lineWidth = 1,
      data = lecand,
      threshold = null
    )
  )
}
