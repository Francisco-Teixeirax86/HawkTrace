package com.hawtrace.processor

import scala.util.{Failure, Success}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import com.hawtrace.processor.models.LogEvent
import com.hawtrace.processor.services.{KafkaService, RuleEngineService}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

object HawkTraceProcessor extends App with LazyLogging{
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "HawkTraceProcessor")
  implicit val ec: ExecutionContext = system.executionContext

  val config = ConfigFactory.load()

  // Initialize services
  val ruleEngineService = new RuleEngineService()
  val kafkaService = new KafkaService(config)

  // Consumer Settings for kafka
  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(config.getString("kafka.bootstrap.servers"))
    .withGroupId(config.getString("kafka.consumer.group-id"))
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  logger.info("Starting HawkTrace Processor....")

  Consumer
    .plainSource(consumerSettings, Subscriptions.topics(config.getString("kafka.topics.logs")))
    .map { record =>
      logger.debug(s"Received message: ${record.value()}")

      // Parse JSON to LogEvent
      Json.parse(record.value()).asOpt[LogEvent] match {
        case Some(logEvent) =>
          logger.debug(s"Parsed log event: ${logEvent.id}")

          // Apply the security rules
          val alerts = ruleEngineService.evaluateEvent(logEvent)

          // Send alerts to kafka topic
          alerts.foreach { alert =>
            kafkaService.publishAlert(alert)
            logger.info(s"Generated alert: ${alert.alertType} from ${alert.sourceIp}")
          }

        case None =>
          logger.warn(s"Failed to parse log event: ${record.value()}")
      }

      record
    }
    .runWith(Sink.ignore)
    .onComplete {
      case Success(_) => logger.info("Stream completed with success")
      case Failure(ex) => logger.error("Stream failed", ex)
    }


  // Graceful shutdown
  sys.addShutdownHook {
    logger.info("Shutting down HawkTrace Processor...")
    system.terminate()
  }

  logger.info("HawkTrace Processor started successfully")
}
