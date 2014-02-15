package se.kodiak.primes.backend.logic

import akka.actor.{Props, Actor}
import se.kodiak.primes.backend.logic.model.Messages
import Messages.{Calculate, StartCalculation}

class IncomingNumberActor extends Actor {
  val primeNumberActor = context.system.actorOf(Props(classOf[PrimeNumberActor]).withDispatcher("my.dispatcher"))

  def receive = {
    case c:StartCalculation => {
      val list = (c.min to c.max).toList
      list.par.foreach { number =>
        primeNumberActor ! new Calculate(c.key, number)
      }
    }
  }
}
