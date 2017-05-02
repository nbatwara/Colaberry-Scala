package KafkaMain

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ConsumerMessage, Subscriptions }
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

/**
  * Created by nehba_000 on 4/29/2017.
  */
object FileWriterSource {

  def create(groupID: String)(implicit system: ActorSystem): Source[ConsumerMessage.CommittableMessage[Array[Byte],String], Consumer.Control] = {
    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId(groupID)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics(FileNumberTopic.Topic))
  }


}
