package com.kaizo.keeper

import akka.actor.{ActorSystem, Props}
import com.kaizo.keeper.actor.KaizoKeeperActor
import com.kaizo.keeper.actor.KaizoKeeperActor.StartCleaning
import com.kaizo.keeper.service.DefaultDbService
import slick.driver.PostgresDriver.api._
import scala.concurrent.duration._

object Boot extends App {

  val dbService = new DefaultDbService(Database.forConfig("db"))
  val system = ActorSystem("Kaizo-Keeper")
  val keeperActor = system.actorOf(Props(new KaizoKeeperActor(dbService)), "keeperActor")

  import system.dispatcher
  system.scheduler.scheduleOnce(5.seconds, keeperActor, StartCleaning)
}
