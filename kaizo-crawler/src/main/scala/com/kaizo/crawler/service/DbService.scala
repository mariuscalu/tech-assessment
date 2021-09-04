package com.kaizo.crawler.service

import com.kaizo.crawler.model._
import slick.driver.PostgresDriver.api._
import com.kaizo.crawler.model.Tables._

import scala.concurrent.Future

trait DbService {

  def geUnlockedDomains: Future[Seq[Leader]]

  def releaseLock(leader: Leader): Future[Unit]

  def acquireLock(leader: Leader): Future[Int]

  def getStatus(domain: String): Future[Option[Status]]

  def getCustomer(domain: String): Future[Option[Customer]]

  def updateStatus(newStatus: Status): Future[Int]

  def sendHealthCheck(leader: Leader): Future[Int]

}

  class DefaultDbService(db: Database) extends DbService {

    def geUnlockedDomains: Future[Seq[Leader]] = {
      db.run(leaders.filter(_.updated.isEmpty).result)
    }

    def acquireLock(leader: Leader): Future[Int] = {
      db.run(leaders.filter(_.domain === leader.domain).filter(_.updated.isEmpty).update(leader))
    }

    def sendHealthCheck(leader: Leader): Future[Int] = {
      db.run(leaders.filter(_.domain === leader.domain).update(leader))
    }

    def releaseLock(leader: Leader): Future[Unit] = {
      val unlockedDomain = Leader(leader.domain, null)
      db.run(leaders.filter(_.domain === leader.domain).delete)
      db.run(DBIO.seq(leaders += unlockedDomain))
    }

    def getStatus(domain: String): Future[Option[Status]] = {
      db.run(statuses.filter(_.domain === domain).result.headOption)
    }

    def updateStatus(newStatus: Status): Future[Int] = {
      db.run(statuses.filter(_.domain === newStatus.domain).update(newStatus))
    }

    def getCustomer(domain: String): Future[Option[Customer]] = {
      db.run(customers.filter(_.domain === domain).result.headOption)
    }
  }
