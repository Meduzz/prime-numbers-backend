package se.kodiak.primes.backend

import akka.actor.{Props, ActorSystem}
import se.kodiak.primes.backend.logic.{IncomingNumberActor, AmqpActor}
import com.typesafe.config.ConfigFactory
import se.kodiak.primes.backend.logic.model.Messages.Listen
import se.kodiak.primes.backend.amqp.AmqpWrapper

object Start {
  def main(args:Array[String]) {
    // Get actor
    val system = ActorSystem()
    val amqpListener = system.actorOf(Props(classOf[AmqpActor]))

    // Get config
    val conf = ConfigFactory.load()
    val server = conf.getString("my.server.host")
    val port = conf.getInt("my.server.port")
    val inQueName = conf.getString("my.server.queue.in")
    val outQueName = conf.getString("my.server.queue.out")

    // get a amqp channel
    val channel = AmqpWrapper(server, port).channel()

    // create an exchange and queue
    channel.withChannel { chan =>
      chan.exchangeDeclare("test", "direct", false)
      chan.queueDeclare(inQueName, false, false, false, null)
      chan.queueDeclare(outQueName, false, false, false, null)
      chan.queueBind(inQueName, "test", "in")
      chan.queueBind(outQueName, "test", "out")
    }

    // start listening
    amqpListener ! Listen(server, port, inQueName)

//    val actor = system.actorOf(Props(classOf[IncomingNumberActor]))
//    actor ! new StartCalculation("asdf", 100000) // takes a while... I suspect it's because System.out are a slowbe.
  }
}
