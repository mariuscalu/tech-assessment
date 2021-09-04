package com.kaizo.rest.service

import com.kaizo.rest.model.{Customer, Status}

import scala.concurrent.Future

trait UserService {

  def getStatus(domain: String): Future[Option[Status]]

  def saveCustomer(customer: Customer): Future[Unit]
}

class DefaultUserService(dbService: DbService) extends UserService{

  def getStatus(domain: String): Future[Option[Status]] = {
    dbService.getStatus(domain)
  }

  def saveCustomer(customer: Customer): Future[Unit] = {
    dbService.saveCustomer(customer)
  }
}