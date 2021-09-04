package com.kaizo.crawler.actor

import java.sql.Timestamp
import java.util.Date

import akka.actor.{Actor, PoisonPill}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.kaizo.crawler.model.{JsonSupport, Leader, Status, ZendeskRecord}
import com.kaizo.crawler.service.DbService

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class CrawlerActor(dbService: DbService) extends Actor with JsonSupport {

  import CrawlerActor._

  implicit val actorMaterializer = ActorMaterializer()
  implicit val system = context.system

  def receive = {
    case LockDomain(domain) => tryToLockDomain(domain)
    case Crawl(domain) => startCrawling(domain)
  }

  def getCurrentTimestamp = {
    val currentDate = new Date()
    new Timestamp(currentDate.getTime())
  }

  def tryToLockDomain(domain: String) = {
    dbService.acquireLock(Leader(domain, Some(getCurrentTimestamp))).onComplete {
      case Success(status) =>
        if (status > 0) context.system.scheduler.schedule(10 second ,10 second, self, Crawl(domain))
        else self ! PoisonPill
      case Failure(err) => {
        println(err.getStackTrace.mkString("\n"))
          self ! PoisonPill
      }
    }
  }

  def startCrawling(domain: String) = {
    val futureToken = getToken(domain)
    val futureURL = getURL(domain)

    val response: Future[Future[HttpResponse]] = for {
      token <- futureToken
      url <- futureURL
    } yield sendRequest(url, token)

    dbService.sendHealthCheck(Leader(domain, Some(getCurrentTimestamp)))

    response.map(printTicket(domain, _))
  }

  def sendRequest(url: String, token: Option[String]): Future[HttpResponse] = {
    val header = headers.Authorization(OAuth2BearerToken(token.get))

    Http()
      .singleRequest(HttpRequest(uri = url, headers = List(header)))
  }

  def getToken(domain: String): Future[Option[String]] =
    dbService.getCustomer(domain).map(_.map(_.token))

  def getURL(domain: String): Future[String] =
    dbService.getStatus(domain).map(status => {
      if (status.get.end.isDefined) {
        val newStatus = Status(domain, status.get.end, None)
        dbService.updateStatus(newStatus)
        s"https://${domain}..zendesk.com/api/v2/incremental/tickets.json?start_time=${status.get.end.get.getTime}"
      } else {
        val currentTimestamp = getCurrentTimestamp
        val newStatus = Status(domain, Some(currentTimestamp), None)
        dbService.updateStatus(newStatus)
        s"https://${domain}..zendesk.com/api/v2/incremental/tickets.json?start_time=${currentTimestamp.getTime}"
      }
    })

  def printTicket(domain: String, response: Future[HttpResponse]) = response.onComplete {
    case Success(res) => {
      val json: Future[ZendeskRecord] = Unmarshal(res.entity).to[ZendeskRecord]
      json.foreach(content => {
        dbService.updateStatus(Status(domain, None, Some(content.end_time)))
        content.tickets.foreach(ticket =>
          println(s"${ticket.id}-${ticket.created_at}-${ticket.updated_at}"))
      })
    }
    case Failure(err) => println(err.getStackTrace.mkString("\n"))
  }
}

object CrawlerActor {

  case class LockDomain(domain: String)
  case class Crawl(domain: String)
}

