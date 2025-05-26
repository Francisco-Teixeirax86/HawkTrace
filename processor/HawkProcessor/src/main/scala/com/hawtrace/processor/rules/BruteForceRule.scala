package com.hawtrace.processor.rules

import com.hawtrace.processor.models.{LogEvent, SecurityAlert}
import com.hawtrace.processor.utils.Constants
import com.typesafe.scalalogging.LazyLogging

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.collection.mutable
import scala.concurrent.duration._

class BruteForceRule extends SecurityRule with LazyLogging{

  //Track failed atttempts per IP (maybe change to use Redis after)
  private val failedAttempts: mutable.Map[String, List[Instant]] = mutable.Map()

  //Configuration
  private val MAX_FAILED_ATTEMPTS = 5
  private val TIME_WINDOW_MINUTES = 5

  override def matches(event: LogEvent): Boolean = {
    val hasAuthAction = event.action.exists(action =>
      action.toLowerCase.contains("login") || action.toLowerCase.contains("auth"))

    val isFailure = event.status.exists(status =>
        status.toLowerCase.contains("fail") ||
        status.toLowerCase.contains("error") ||
          event.statusCode.exists(_ >= 400)
    )

    hasAuthAction && isFailure
  }

  override def evaluate(event: LogEvent): List[SecurityAlert] = {
    event.sourceIp match {
      case Some(sourceIp) =>
        val now = Instant.now()

        //Clean old entries and add new failure
        cleanOldEntries(sourceIp, now)
        addFailedAttemp(sourceIp, now)

        failedAttempts.get(sourceIp) match {
          case Some(attempts) if attempts.length >= MAX_FAILED_ATTEMPTS =>
            logger.warn(s"BRUTE FORCE DETECTED: ${attempts.length} attempts from $sourceIp")

            val alert = SecurityAlert(
              alertType = Constants.BRUTE_FORCE,
              severity = Constants.HIGH,
              title = "Brute Force Attack Detected",
              description = s"Detected ${attempts.length} failed to login attempts from IP $sourceIp",
              sourceIp = Some(sourceIp),
              username = event.username,
              source = Some(event.source),
              eventCount = Some(attempts.length),
              metadata = Map(
                "first_attempt" -> attempts.head.toString,
                "last_attempt" -> attempts.last.toString,
                "time_window_minutes" -> TIME_WINDOW_MINUTES.toString,
                "threshold" -> MAX_FAILED_ATTEMPTS.toString
              ),
              ruleName = ruleName
            )

          //Reset counter after alert
          failedAttempts.remove(sourceIp)

          List(alert)

          case _ => List.empty
        }
      case None =>
        logger.debug("No source IP in event, skipping brute force check")
        List.empty
    }

  }

  private def addFailedAttemp(sourceIp: String, timestamp: Instant): Unit = {
    val current = failedAttempts.getOrElse(sourceIp, List.empty)
    failedAttempts(sourceIp) = current :+ timestamp
  }

  private def cleanOldEntries(sourceIp: String, now: Instant): Unit = {
    failedAttempts.get(sourceIp) match {
      case Some(attempts) =>
        val recent = attempts.filter { attempt =>
          ChronoUnit.MINUTES.between(attempt, now) <= TIME_WINDOW_MINUTES
        }

        if (recent.nonEmpty) {
          failedAttempts(sourceIp) = recent
        } else {
          failedAttempts.remove(sourceIp)
        }
      case None => // No entries to clean
    }
  }

  override def ruleName: String = Constants.BruteForceDetection

  override def priority: Int = 10 //High
}
