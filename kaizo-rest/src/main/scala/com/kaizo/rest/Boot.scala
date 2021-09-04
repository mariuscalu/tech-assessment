package com.kaizo.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.kaizo.rest.config.AppConfig._
import com.kaizo.rest.service.{DefaultDbService, DefaultUserService, RestService}
import slick.driver.PostgresDriver.api._


object Boot extends App {

  implicit val actorSystem = ActorSystem("system")
  implicit val actorMaterializer = ActorMaterializer()

  val restService = new RestService(new DefaultUserService(new DefaultDbService(Database.forConfig("db"))));

  Http().bindAndHandle(restService.routes, serverHost, serverPort)
}
