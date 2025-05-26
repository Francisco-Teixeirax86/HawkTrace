package com.hawtrace.processor.services

import com.hawtrace.processor.models.{LogEvent, SecurityAlert}
import com.hawtrace.processor.rules.{BruteForceRule, SecurityRule}
import com.typesafe.scalalogging.LazyLogging

class RuleEngineService extends LazyLogging{

  //TODO Initialize rules (to change later to ble able to configure on the front end)
  private val rules: List[SecurityRule] = List(
    new BruteForceRule()
    //...
  ).sortBy(_.priority)

  logger.info(s"Initialized RuleEngineService with ${rules.length} rules: ${rules.map(_.ruleName)}.mkString(", ")}")

  def evaluateEvent(event: LogEvent): List[SecurityAlert] = {
    rules.flatMap { rule =>
      try {
        if (rule.matches(event)) {
          logger.debug(s"Rule ${rule.ruleName} matches event ${event.id}")
          rule.evaluate(event)
        } else {
          List.empty
        }
      } catch {
        case ex: Exception =>
          logger.error(s"Error evaluating rule ${rule.ruleName}", ex)
          List.empty
      }
    }
  }
}
