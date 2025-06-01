package com.hawtrace.processor.services
import akka.actor.typed.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import com.hawtrace.processor.models.SecurityAlert
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json.Json

class KafkaService(config: Config)(implicit system: ActorSystem[Nothing]) extends LazyLogging{

  private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(config.getString("kafka.bootstrap.servers"))

  private val alertsTopic = config.getString("kafka.topics.alerts")

  def publishAlert(alert: SecurityAlert): Unit = {
    try {
      val alertJson = Json.toJson(alert).toString()
      val record = new ProducerRecord[String, String](alertsTopic, alert.id, alertJson)

      Source.single(record).runWith(Producer.plainSink(producerSettings))

      logger.info(s"Published alert ${alert.id} to Kafka topic $alertsTopic")
    } catch {
      case ex: Exception =>
        logger.error(s"Failed to publish alert ${alert.id} to Kafka", ex)
    }
  }
}

