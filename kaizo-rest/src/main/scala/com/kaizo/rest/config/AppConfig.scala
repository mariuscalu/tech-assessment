package com.kaizo.rest.config

import com.typesafe.config.ConfigFactory

object AppConfig {

  val config = ConfigFactory.load()

  val serverHost = config.getString("server.host")
  val serverPort = config.getInt("server.port")

}

