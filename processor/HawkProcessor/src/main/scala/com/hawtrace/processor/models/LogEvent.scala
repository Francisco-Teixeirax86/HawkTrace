package com.hawtrace.processor.models

import play.api.libs.json.{Format, Json}
import java.time.Instant

case class LogEvent(
  id: String,
  source: String,
  rawContent: String,
  timestamp: Instant,
  collectionTime: Instant,
  logType: String,
  parserType: String,
  hostName: String,
  sourceIp: Option[String] = None,
  destinationIp: Option[String] = None,
  username: Option[String] = None,
  action: Option[String] = None,
  status: Option[String] = None,
  statusCode: Option[Int] = None,
  message: Option[String] = None,
  severity: Option[String] = None,
  userAgent: Option[String] = None,
  url: Option[String] = None,
  responseSize: Option[Long] = None,
  additionalFields: Option[Map[String, String]] = None
)

object LogEvent {
  implicit val format: Format[LogEvent] = Json.format[LogEvent]
}