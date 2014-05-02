package lostvaults.client
import akka.io.{ IO, Tcp }
import akka.actor.{ Actor, ActorRef, Props }
import java.net.InetSocketAddress
import scala.concurrent.duration._
import lostvaults.Parser
import akka.util.ByteString
sealed trait MyMsg
case class Print(msg: String) extends MyMsg
case object ConnClosed extends MyMsg
case object ShutDown extends MyMsg
case object Ok extends MyMsg

object TCPClientxxx {
  def props(listener: ActorRef): Props = Props(new TCPClientxxx(listener))
}

class TCPClientxxx(listener: ActorRef) extends Actor {
  import Tcp._
  import context.system
  val manager = IO(Tcp)
  var connection: Option[ActorRef] = None

  override def preStart() = {
    manager ! Connect(new InetSocketAddress("localhost", 51234))
    // Ändra localhost i slutversionen till IP'n för Servern.
  }

  def receive = {
    case CommandFailed(_: Connect) => {
      listener ! "Connect failed"
      context stop self
    }
    case c @ Connected(remote, local) => {
      listener ! "Connected"
      connection = Some(sender)
      sender ! Register(self)
    }
    //sender ! Write(ByteString("Login Jimmy"))
    case Received(c) => {
      val msg = c.decodeString(java.nio.charset.Charset.defaultCharset().name())
      println("Received message from server: " + msg)
      listener ! msg
    }
    case msg: String =>
      connection.get ! Write(ByteString(msg))
    case x: ConnectionClosed => {
      println("Connection closed - shutting down.")
      listener ! x.getErrorCause
      self ! ShutDown
    }
    case _ =>
      println("other")
  }
}