package com.hawtrace.processor.rules

import com.hawtrace.processor.models.{LogEvent, SecurityAlert}

trait SecurityRule {

  /**
   * Check if this rule applies to the given log event
   */
  def matches(event: LogEvent): Boolean

  /**
   * Evaluate the event and potentially generate alerts
   */
  def evaluate(event: LogEvent): List[SecurityAlert]

  /**
   * Get the name of this rule
   */
  def ruleName: String

  /**
   * Get the priority of this rule (lower number = higher priority)
   */
  def priority: Int = 100
}
