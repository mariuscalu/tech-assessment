package com.kaizo.rest.service

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.kaizo.rest.model.{Customer, JsonSupport}

import scala.concurrent.Future

class RestService(userService: UserService) extends SprayJsonSupport with JsonSupport{

  val routes: Route =
    get {
      path("status" / Segment) { domain =>
        rejectEmptyResponse {
          complete {
            userService.getStatus(domain)
          }
        }
      }
    } ~ post {
      path("customers") {
        entity(as[Customer]) { customer =>
          val savedUser: Future[Unit] = userService.saveCustomer(customer)
          onSuccess(savedUser) {
            complete("customer has been saved")
          }
        }
      }
    }
}