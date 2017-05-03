package KafkaMain

/**
  * Created by nehba_000 on 5/2/2017.
  */

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

class KafkaToKafka extends  Actor with ActorLogging  {
  import KafkaToKafka._

  override def preStart(): Unit ={
    super.preStart()
    self ! Run
  }

  override def postStop(): Unit = {
    super.postStop()
    println("Producer stopped")
  }

  override def receive: Receive = {
    case Run =>

      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("192.168.99.100:9092")
      val kafkaSink = Producer.plainSink(producerSettings)
      implicit  val mat =ActorMaterializer()
      val consumerSettings = ConsumerSettings(context.system, new ByteArrayDeserializer, new StringDeserializer)
        .withBootstrapServers("192.168.99.100:9092")
        .withGroupId("groupID")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      Consumer.committableSource(consumerSettings, Subscriptions.topics(FileNumberTopic.Topic))
        .map { msg =>
          println(s"topic1 -> topic2: $msg")
          val processedmsg=msg.record.value.toUpperCase()
          ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
            "TopicB",
            processedmsg
          ), msg.committableOffset)
        }
        .runWith(Producer.commitableSink(producerSettings))
  }


}


object KafkaToKafka extends App {
  case object Run
  case object Stop
  val system =ActorSystem("qtoq")
  type Message = CommittableMessage[Array[Byte], String]
  val fc=system.actorOf(Props[KafkaToKafka], name="quetoque")
  println("Done!!")
}