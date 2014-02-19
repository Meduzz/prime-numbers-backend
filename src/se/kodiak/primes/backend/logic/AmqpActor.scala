package se.kodiak.primes.backend.logic

import akka.actor.{ActorLogging, Props, Actor}
import se.kodiak.primes.backend.amqp.AmqpWrapper
import com.rabbitmq.client.Envelope
import se.kodiak.primes.backend.logic.model.Messages.{StartCalculation, Listen}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}

class AmqpActor extends Actor with ActorLogging {

  implicit val formats = Serialization.formats(NoTypeHints)

  def receive = {
    case l:Listen => {
      val wrapper = AmqpWrapper(l.server, l.port).channel()
      wrapper.listen(l.queName, "", callback)
      log.info("Listening on AMQP")
    }
  }

  def callback(envelope:Envelope, body:Array[Byte]):Unit = {
    log.info(s"Received new message from AMQP (${body.length} bytes) (${new String(body, "utf-8")})")

    val numberActor = context.system.actorOf(Props(classOf[IncomingNumberActor]))
    val msg:StartCalculation = read[StartCalculation](new String(body, "utf-8"))

    numberActor ! msg
  }
}
