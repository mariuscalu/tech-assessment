package com.kaizo.keeper.model

import java.sql.Timestamp
import slick.driver.PostgresDriver.api._

final case class Leader(domain: String, updated: Option[Timestamp])

object Tables {
  class Leaders(tag: Tag ) extends Table[Leader](tag, "leader") {

    def domain = column[String]("domain")

    def updated = column[Option[Timestamp]]("updated")

    def * = (domain, updated) <> (Leader.tupled, Leader.unapply)

  }

  val leaders = TableQuery[Leaders]
}
