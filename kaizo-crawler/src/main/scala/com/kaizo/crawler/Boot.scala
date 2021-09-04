package com.kaizo.crawler

import akka.actor.{ActorSystem, Props}
import com.kaizo.crawler.actor.LeaderActor
import com.kaizo.crawler.actor.LeaderActor.StartCrawling
import com.kaizo.crawler.service.DefaultDbService
import slick.driver.PostgresDriver.api._
import scala.concurrent.duration._

object Boot extends App {

  val dbService = new DefaultDbService(Database.forConfig("db"))
  val system = ActorSystem("Kaizo-Crawler")
  val keeperActor = system.actorOf(Props(new LeaderActor(dbService)), "leaderActor")

  import system.dispatcher
  system.scheduler.schedule(5.seconds, 10.minutes, keeperActor, StartCrawling)
}
