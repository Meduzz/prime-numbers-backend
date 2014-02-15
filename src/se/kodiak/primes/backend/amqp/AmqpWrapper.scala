package se.kodiak.primes.backend.amqp

import com.rabbitmq.client.{Connection => AmqpConnection, Channel => AmqpChannel, AMQP, Envelope, DefaultConsumer, ConnectionFactory}

object AmqpWrapper {
  private var con:Option[AmqpConnection] = None

  def apply(server:String, port:Int):Connection = {
    new ConnectionImpl(con match {
      case Some(c:AmqpConnection) => c
      case None => {
        val factory = new ConnectionFactory()
        factory.setHost(server)
        factory.setPort(port)
        val c = factory.newConnection()
        con = Some(c)
        c
      }
    })
  }
}

trait Connection {
  def channel():Channel
}

private class ConnectionImpl(val connection:AmqpConnection) extends Connection {
  def channel():Channel = {
    new ChannelImpl(connection.createChannel())
  }
}

trait Channel {
  def send(exchange:String, routingKey:String, message:String):Unit
  def listen(queName:String, consumerTag:String, f:(Envelope, Array[Byte])=>Unit):Unit
  def withChannel(f:(AmqpChannel)=>Unit):Unit
}

private class ChannelImpl(val channel:AmqpChannel) extends Channel {

  override def withChannel(f: (AmqpChannel) => Unit): Unit = f(channel)

  override def listen(queName: String, consumerTag: String, f: (Envelope, Array[Byte]) => Unit): Unit = {
    channel.basicConsume(queName, true, consumerTag, new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag:String, envelope:Envelope, props:AMQP.BasicProperties, body:Array[Byte]) {
        f(envelope, body)
      }
    })
  }

  override def send(exchange: String, routingKey: String, message: String): Unit = {
    channel.basicPublish(exchange, routingKey, null, message.getBytes("utf-8"))
  }
}