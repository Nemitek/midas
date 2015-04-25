package com.investek

import java.time.{ZoneOffset, LocalDate}

import spray.http._

import scala.util.{Success, Failure}
import scala.concurrent._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.event.Logging
import akka.io.IO
import spray.json.{JsonFormat, DefaultJsonProtocol}
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._

case class Elevation(location: Location, elevation: Double)
case class Location(lat: Double, lng: Double)
case class GoogleApiResult[T](status: String, results: List[T])

object ElevationJsonProtocol extends DefaultJsonProtocol {
  implicit val locationFormat = jsonFormat2(Location)
  implicit val elevationFormat = jsonFormat2(Elevation)
  implicit def googleApiResultFormat[T :JsonFormat] = jsonFormat2(GoogleApiResult.apply[T])
}

case class Quantile(date: LocalDate, open: BigDecimal, high: BigDecimal, low: BigDecimal, close: BigDecimal, volume: Long, adjustedClose: BigDecimal)

object Main extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("midas")
  import system.dispatcher // execution context for futures below
//  val log = Logging(system, getClass)

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  val response: Future[HttpResponse] = pipeline(Get("http://real-chart.finance.yahoo.com/table.csv?s=%5EGSPC&a=00&b=3&c=1950&d=03&e=17&f=2015&g=d&ignore=.csv"))
  
  response.onComplete{
    case Success(s) =>
      println(s.entity.asString.split("\n").map(x => {
        val parsedArray = x.split(",")
        Quantile(
          LocalDate.parse(parsedArray(0)),

        )
      }))
//      s.entity.asString.split("\n").foreach(line => println(123 + line))
//      println(s.entity.asString)
      system.shutdown()
    case Failure(e) => 
      println("Error")
      system.shutdown()
  }
  
  
  
//  log.info("Requesting the elevation of Mt. Everest from Googles Elevation API...")
//
//  import ElevationJsonProtocol._
//  import SprayJsonSupport._
//  val pipeline = sendReceive ~> unmarshal[GoogleApiResult[Elevation]]
//
//  val responseFuture = pipeline {
//    Get("http://maps.googleapis.com/maps/api/elevation/json?locations=27.988056,86.925278&sensor=false")
//  }
//  responseFuture onComplete {
//    case Success(GoogleApiResult(_, Elevation(_, elevation) :: _)) =>
//      log.info("The elevation of Mt. Everest is: {} m", elevation)
//      shutdown()
//
//    case Success(somethingUnexpected) =>
//      log.warning("The Google API call was successful but returned something unexpected: '{}'.", somethingUnexpected)
//      shutdown()
//
//    case Failure(error) =>
//      log.error(error, "Couldn't get elevation")
//      shutdown()
//  }

//  def shutdown(): Unit = {
//    IO(Http).ask(Http.CloseAll)(1.second).await
//    system.shutdown()
//  }
}