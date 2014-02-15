package se.kodiak.primes.backend.logic.model

object Messages {
  case class StartCalculation(key:String, max:Long, min:Long = 0L)
  case class Calculate(key:String, number:Long)
  case class Listen(server:String, port:Int, queName:String)
}
