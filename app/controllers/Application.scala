package controllers

import play.api._
import play.api.mvc._

import play.api.libs.concurrent.Execution._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import akka.actor._
import akka.event._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._


class Application extends Controller {

  private val delay = 10
  private def now = new java.util.Date().toString

  def index = Action {
    println(s"hello ${now}")
    Ok("hello")
  }


  def sleep = Action {
    println(s"sleep start ${now}")
    Thread.sleep(1000 * delay)
    val end = now
    println(s"sleep end ${end}")
    Ok(end)
  }

  

  private val config = com.typesafe.config.ConfigFactory.load.getConfig("sample-context")
  private val system = ActorSystem("SampleActor", config)
  private def actor:ActorRef = system.actorOf(Props[SampleActor])
  private implicit val timeout = Timeout(20 second)

  def asyncSleep = Action.async {
    println(s"asyncSleep start ${now}")
    val result = actor ? TimeMessage("hello")
    result.map { message => 
      val ret = message.asInstanceOf[String]
      println(s"sleepSleep end ${ret}")
      Ok(ret)
    }
  }
}

case class TimeMessage(message: String)

class SampleActor extends Actor with ActorLogging {
  // import context._

  def receive = {
    case TimeMessage(message: String) => getTime(message)
    case _ => throw new RuntimeException
  }

  //時間のかかる処理
  def getTime(message:String) = {
    println(s"async ${message}")
    Thread.sleep(1000 * 5)
    sender ! new java.util.Date().toString
  }
}
