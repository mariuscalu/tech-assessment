package com.kaizo.keeper.actor

import java.sql.Timestamp
import java.util.Date
import akka.actor.Actor
import com.kaizo.keeper.model.Leader
import com.kaizo.keeper.service.DbService
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class KaizoKeeperActor(dbService: DbService) extends Actor {

  import KaizoKeeperActor._

  def receive = {
    case StartCleaning =>
      val lockedDomains = dbService.getLockedDomains
      processDomains(lockedDomains)
  }

  def selectBlockedDomains(records: Seq[Leader]): Seq[Leader] = {

    val currentDate = new Date()
    val currentTimestamp = new Timestamp(currentDate.getTime())

    def lastUpdate(lastUpdate: Timestamp): Int = {
      val diff = currentTimestamp.getTime - lastUpdate.getTime
      val differenceInHours = diff / 3600000
      differenceInHours.toInt
    }

    records.filter(leader => lastUpdate(leader.updated.getOrElse(currentTimestamp)) >= 1)
  }

  def unlockDomains(futureRecords: Future[Seq[Leader]]) = futureRecords.onComplete {
    case Success(seq) => seq.map(record => dbService.releaseLock(record))
    case Failure(err) => println(err.getStackTrace.mkString("\n"))
  }

  def processDomains(futureRecords: Future[Seq[Leader]]) = {
    val blockedDomains = futureRecords.map(selectBlockedDomains(_))
    unlockDomains(blockedDomains)
  }

}

object KaizoKeeperActor {

  case object StartCleaning
}
