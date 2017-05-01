package KafkaMain


import akka.actor.{Actor, ActorLogging}
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by nehba_000 on 4/29/2017.
  */

class FileConsumer(implicit mat: Materializer) extends Actor with ActorLogging {

  import FileConsumer._


//  val producerSetting2 = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer).withBootstrapServers("localhost:9092")
//
//  val kafkaSink = Producer.plainSink(producerSetting2)

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      log.info("Initializing File Consumer.....")
      val (control, future) = FileWriterSource.create("FileConsumer")(context.system)
        .mapAsync(2)(processMessage)
        .map(_.committableOffset)
        .groupedWithin(10, 15 seconds)
        .map(group => group.foldLeft(CommittableOffsetBatch.empty) { (batch, elem) => batch.updated(elem) })
        .mapAsync(1)(_.commitScaladsl())
        .toMat(Sink.ignore)(Keep.both)
      .run()

    context.become(running(control))

//   future.onComplete {
//      case Error(res) => {
//        log.error("Stream failed due to error, restarting")
//        println("****Error****" + res)
//      }
//      case _ =>
//        log.info("Logging Consumer Started")
//   }


  }
  def running(control: Control): Receive = {
    case Stop =>
      log.info("Shutting down logging consumer stream and actor")
      control.shutdown().andThen {
        case _ =>
          context.stop(self)
      }
  }

  private def processMessage(msg: Message): Future[Message] = {
    log.info(s"Consumed number: ${msg.record.value()}")
    Future.successful(msg)
  }
}

object FileConsumer{
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop
}