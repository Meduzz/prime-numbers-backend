package se.kodiak.primes.backend.logic.model

import org.json4s._

object Messages {
  case class StartCalculation(min:Long, max:Long, key:String)
  case class Calculate(key:String, number:Long)
  case class Listen(server:String, port:Int, queName:String)
}
