

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.event.Logging
import akka.event.slf4j.Logger
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.{ ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.ConsumerSettings
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

import scala.concurrent.Future

/**
  * Created by nehba_000 on 5/1/2017.
  */

object DataConsumer extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  type Message = CommittableMessage[Array[Byte], String]
  case object Consume
  case object StopConsume

  val initialActor = classOf[DataConsumer].getName
  akka.Main.main(Array(initialActor))
}

class DataConsumer extends Actor with ActorLogging {

  import DataConsumer._

  override def preStart(): Unit = {
    super.preStart()
    self ! Consume
  }
  override def receive: Receive = {
    case Consume => {
      Logger("Initializing Source for kafka consumer")

      val (control, future) = KafkaConsumerSource.create("DataConsumer")(context.system)
      .mapAsync(2)(processMessage)
      .toMat(Sink.ignore)(Keep.both)
      .run()

      Logger("Initializing sink settings for producer")
      /**
        * Initialize setting so producer can take record of new topic
        */
      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers(config.getString("kafka.broker-list"))

      Logger("Materializing Stream")

      val kafkaSink = Producer.plainSink(producerSettings)

      val source = Consumer.committableSource(consumerSettings ,Subscriptions.topics(config.getString("kafka.topic1")))
        .mapAsync(3)(processMessage)
        .via(Producer.flow(producerSettings))
        .map(_.message.passThrough)
        .groupedWithin(10, 15 seconds)
        .map(group=>group.foldLeft(CommittableOffsetBatch.empty){(batch,elem)=>batch.updated(elem)})
        .mapAsync(1)(_.commitScaladsl())

      val done = source.runWith(Sink.ignore)

    }
  }

  def processMessage(msg:ConsumerActor.Message) = {

    log.info(s"Consumed number: ${msg.record.value()}")
    Future.successful(msg)
  }


}
