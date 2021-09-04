package com.kaizo.crawler.actor

import akka.actor.{Actor, Props}
import com.kaizo.crawler.actor.CrawlerActor.LockDomain
import com.kaizo.crawler.service.DbService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class LeaderActor(dbService: DbService) extends Actor  {
  import LeaderActor._

  implicit val system = context.system

  def receive = {
    case StartCrawling => scheduleWork
  }

  def getCrawlerActor(domain: String) = system.actorOf(Props(new CrawlerActor(dbService)), s"crawler-$domain")

  def scheduleWork =
    dbService.geUnlockedDomains.onComplete {
      case Success(records) => records.foreach(record => {
        val crawlerActor = getCrawlerActor(record.domain)
        crawlerActor ! LockDomain(record.domain)
      })
      case Failure(err) => println(err.getStackTrace.mkString("\n"))
    }
}

object LeaderActor {

  case object StartCrawling
}
