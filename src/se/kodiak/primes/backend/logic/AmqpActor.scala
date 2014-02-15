package se.kodiak.primes.backend.logic

import akka.actor.{ActorLogging, Props, Actor}
import se.kodiak.primes.backend.amqp.AmqpWrapper
import com.rabbitmq.client.Envelope
import se.kodiak.primes.backend.logic.model.Messages.{StartCalculation, Listen}
import org.json4s._
import org.json4s.native.JsonMethods._

class AmqpActor extends Actor with ActorLogging {

  def receive = {
    case l:Listen => {
      val wrapper = AmqpWrapper(l.server, l.port).channel()
      wrapper.listen(l.queName, "", callback)
      log.info("Listening on AMQP")
    }
  }

  def callback(envelope:Envelope, body:Array[Byte]):Unit = {
    log.info("Received new message from AMQP")

    val numberActor = context.system.actorOf(Props(classOf[IncomingNumberActor]))

    val json = parse(new String(body, "utf-8"))

    // TODO there's an optional min number in this message too.
    val list:List[(String, Long)] = for {
      JObject(obj) <- json
      JField("key", JString(key)) <- obj
      JField("max", JInt(number)) <- obj
    } yield (key, number.toLong)

    val data = list.head
    numberActor ! new StartCalculation(data._1, data._2)
  }
}
