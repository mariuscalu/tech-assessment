package com.kaizo.rest.model

import java.sql.Timestamp

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import slick.driver.PostgresDriver.api._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsValue, JsonFormat, RootJsonFormat}

final case class Customer(domain: String, name: String, token: String)
final case class Status(domain: String, start: Option[Timestamp], end: Option[Timestamp])
final case class Leader(domain: String, updated: Timestamp)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    def write(obj: Timestamp): JsNumber = JsNumber(obj.getTime)

    def read(json: JsValue): Timestamp = json match {
      case JsNumber(time) => new Timestamp(time.toLong)
      case _ => throw DeserializationException("Date expected")
    }
  }

  implicit val customerFormat: RootJsonFormat[Customer] = jsonFormat3(Customer)
  implicit val statusFormat: RootJsonFormat[Status] = jsonFormat3(Status)
  implicit val leaderFormat: RootJsonFormat[Leader] = jsonFormat2(Leader)
}

object Tables {
  class Customers(tag: Tag ) extends Table[Customer](tag, "customer") {

    def domain = column[String]("domain")

    def name = column[String]("name")

    def token = column[String]("token")

    def * = (domain, name, token) <> (Customer.tupled, Customer.unapply)
  }

  class Statuses(tag: Tag ) extends Table[Status](tag, "status") {

    def domain = column[String]("domain")

    def start = column[Option[Timestamp]]("start")

    def end = column[Option[Timestamp]]("end")

    def * = (domain, start, end) <> (Status.tupled, Status.unapply)

  }

  class Leaders(tag: Tag ) extends Table[Leader](tag, "leader") {

    def domain = column[String]("domain")

    def updated = column[Timestamp]("updated")

    def * = (domain, updated) <> (Leader.tupled, Leader.unapply)

  }

  val customers = TableQuery[Customers]
  val statuses = TableQuery[Statuses]
  val leaders = TableQuery[Leaders]

}
