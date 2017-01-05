import com.highcharts.HighchartsUtils._
import org.scalajs.dom._
import org.scalajs.dom.WebSocket
import org.scalajs.jquery._
import upickle.default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import js.Dynamic.{global => g}
import scala.scalajs.js.annotation.JSExport
import chart.ClientRoutes
import common.models.halo.EventData
import upickle.Invalid.Json

import scala.scalajs.js.JSON

object MarketUpdater {

  @JSExport
  def connect(marketName: String): Unit = {
    // TODO pull from jsRoutes
    //val url = ClientRoutes.controllers.Arbiter.sportMatchData(marketName).url.asInstanceOf[String]
    val url = "undefined"

    val socket = new WebSocket(s"ws://localhost:9000$url")

    socket.onopen = (x: Event) => {
      println("connected")
    }

    socket.onclose = (x: Event) => {
      println("connection closed")
    }

    socket.onmessage = receive _
  }

  val chart = HighchartsJQuery(jQuery("#candle-chart")).highcharts()

  //override def close() = socket.close()
  def receive(e: MessageEvent) = {
    val read = upickle.json.read(e.data.toString)
    val t = read.obj("type").toString()

    t.replaceAll("\"", "") match {
      case "TestPilot" =>
        println("roger test success!")
      case x =>
        println(x)
    }
  }
}

/**
  * French lesson 1/1/16: La Marche - market.
  */
@JSExport
object ArbiterSportMatch {

  @JSExport
  def connect() = {
    BookLineChart.loadSampleData().map { data ⇒
      val str =  js.JSON.stringify(data)
      val dat = upickle.default.read[EventData](str)
      println(dat.odds)
      jQuery("#line-chart").highstock(new BookLineChart(dat))
    }
    //val matchName = jQuery("#match-name").html()
    // init the candle chart
  }
}
