package se.kodiak.primes.backend.logic

import akka.actor.{ActorLogging, Actor}
import se.kodiak.primes.backend.logic.model.Messages.Calculate
import se.kodiak.primes.backend.amqp.AmqpWrapper
import com.typesafe.config.ConfigFactory
import org.json4s._
import org.json4s.native.JsonMethods._

class PrimeNumberActor extends Actor with ActorLogging {
  val conf = ConfigFactory.load()
  val server = conf.getString("my.server.host")
  val port = conf.getInt("my.server.port")
  val outQueyName = conf.getString("my.server.queue.out")

  val channel = AmqpWrapper(server, port).channel()

  def receive = {
    case m:Calculate => {
      val numbers = (2L to m.number).toList
      val result = numbers.par.filter{ i =>
        // remove all "candidates" from number that does not divide even (without reminders) with m.number.
        m.number % i == 0
      }.par.count{ i =>
        // only count divisions that return a "whole" number, Dejavu of filter above?
        m.number.toDouble./(i.toDouble).isWhole()
      }

      if (result == 1) {
        // log and spam
        log.info(s"Found a prime (${m.number})")
        channel.send("test", "out", compact(render(m)).getBytes("utf-8"))
      }
    }
  }
}
