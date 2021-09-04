package com.kaizo.keeper.service

import com.kaizo.keeper.model.Leader
import slick.driver.PostgresDriver.api._
import com.kaizo.keeper.model.Tables._

import scala.concurrent.Future

trait DbService {

  def getLockedDomains: Future[Seq[Leader]]

  def releaseLock(leader: Leader): Future[Unit]

}

class DefaultDbService(db: Database) extends DbService {

  def getLockedDomains: Future[Seq[Leader]] = {
    db.run(leaders.filter(_.updated.isDefined).result)
  }

  def releaseLock(leader: Leader): Future[Unit] = {
    val unlockedDomain = Leader(leader.domain, null)
    db.run(leaders.filter(_.domain === leader.domain).delete)
    db.run(DBIO.seq(leaders += unlockedDomain))
  }
}
