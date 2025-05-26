package com.hawtrace.processor.models

import play.api.libs.json.{Format, Json}
import java.time.Instant
import java.util.UUID

case class SecurityAlert (
  id: String = UUID.randomUUID().toString,
  alertType: String,
  severity: String,
  title: String,
  description: String,
  timestamp: Instant = Instant.now(),
  sourceIp: Option[String] = None,
  username: Option[String] = None,
  source: Option[String] = None,
  eventCount: Option[Int] = None,
  status: String = "OPEN",
  metadata: Map[String, String] = Map.empty,
  ruleName: String
)

object SecurityAlert {
  implicit val format: Format[SecurityAlert] = Json.format[SecurityAlert]
}