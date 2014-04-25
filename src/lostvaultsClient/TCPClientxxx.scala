package lostvaultsClient

import akka.io.{ IO, Tcp }
import akka.actor.{ Actor, ActorRef, Props }
import akka.util.Timeout
import akka.pattern.ask
import akka.util.ByteString
import scala.concurrent.Await
import java.net.InetSocketAddress
import scala.concurrent.duration._

sealed trait MyMsg
case class Print(msg: String) extends MyMsg
case object ConnClosed extends MyMsg
case object ShutDown extends MyMsg
case object Ok extends MyMsg

object TCPClientxxx {
  def props: Props = Props(new TCPClientxxx)
}

class TCPClientxxx extends Actor {
  import Tcp._
  import context.system
  val manager = IO(Tcp)
  var connection: Option[ActorRef] = None
  
  override def preStart() = {
    manager ! Connect(new InetSocketAddress("localhost", 1337))
  }
  
  def receive = {
    case Received(msg) => {
      val dec = msg.decodeString(java.nio.charset.Charset.defaultCharset().name())
      val check = dec.substring(0, 5)
      println("Received message \"%s\"".format(check))
      if (check == "Print")
        println(dec.substring(6, msg.length))
    }
    case Print(msg) =>
      if (!(connection.isEmpty)) {
        connection.get ! Write(ByteString(" says: " + msg))
      }
    case c @ Connected(remote, local) => {
      connection = Some(sender)
      sender ! Register(self)
      println("Connected")
    }
    case CommandFailed(_: Connect) => {
      println("Failed to connect. Shutting Down")
      context stop self
    }
    case ShutDown => {
      implicit val timeout = Timeout(5.seconds)
      connection.get ! ConfirmedClose
    }
    case ConfirmedClosed => {
      context stop self
    }
    case PeerClosed => {
      implicit val timeout = Timeout(5.seconds)
      val future = ask(connection.get, Close)
      Await.result(future, timeout.duration)
    }
    case _: ConnectionClosed => {
      println("Connection closed - shutting down.")
      self ! ShutDown
    }
  }
}