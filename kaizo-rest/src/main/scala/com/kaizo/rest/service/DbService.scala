package com.kaizo.rest.service

import com.kaizo.rest.model.{Customer, Leader, Status}
import slick.driver.PostgresDriver.api._
import com.kaizo.rest.model.Tables._

import scala.concurrent.Future

trait DbService {

  def getStatus(domain: String): Future[Option[Status]]

  def saveCustomer(customer: Customer): Future[Unit]
}

class DefaultDbService(db: Database) extends DbService {

  def saveCustomer(customer: Customer): Future[Unit] = {
    val status = Status(customer.domain, null, null)
    val leader = Leader(customer.domain, null)

    db.run(DBIO.seq(customers += customer))
    db.run(DBIO.seq(statuses += status))
    db.run(DBIO.seq(leaders += leader))
  }

  def getStatus(domain: String): Future[Option[Status]] = {
    db.run(statuses.filter(_.domain === domain).result.headOption)
  }
}
