package KafkaMain
import java.io.{BufferedReader, FileReader}

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.event.Logging
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Source}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.Promise
/**
  * Created by nehba_000 on 4/28/2017.
  */
object FileWriter extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  case object Run
  case object Stop

  val initialActor = classOf[FileWriter].getName
  akka.Main.main(Array(initialActor))
}

class FileWriter extends Actor with ActorLogging {
  import FileWriter._
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
//      val fileSource = Source.unfoldResource[String, BufferedReader](
//          () => new BufferedReader(new FileReader("sample_info.csv")),
//        reader => Option(reader.readLine()),
//        reader => reader.close())
      log.info("Initializing...Reading from source file...")
      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("192.168.99.100:9092")
      val kafkaSink = Producer.plainSink(producerSettings)

      val fileSource = "C:\\Users\\nehba_000\\Desktop\\FileLoaderMicroService\\sample_info.csv"

  Source.unfoldResourceAsync[String, BufferedReader](
  () => Promise.successful(new BufferedReader(new FileReader(fileSource))).future,
  reader => Promise.successful(Option(reader.readLine())).future,
  reader => {
    reader.close()
    self ! Stop
    Promise.successful(Done).future
  }).map( {
      new ProducerRecord[Array[Byte], String](FileNumberTopic.Topic, _)
        })
    .toMat(kafkaSink)(Keep.both)
    .run()
  //(new ProducerRecord[Array[Byte], String](FileNumberTopic.Topic, _)).toMat(kafkaSink)(Keep.both).run()

      //val (control, future) = fileSource
//        future.onFailure {
//          case exception1 =>
//            log.error("Stream failed due to error, restarting", exception1)
//            throw exception1
//        }
//      context.become(running(control))
      log.info(s"Writer now running, writing random numbers to topic ${FileNumberTopic.Topic}")
      Console println("Writing*************")
  }

//  def running(control: Cancellable): Receive = {
//    case Stop => {
//      log.info("Stopping Kafka producer stream and actor")
//      control.cancel()
//      context.stop(self) }
//  }


}
