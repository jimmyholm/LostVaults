package lostvaults
import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress

sealed trait ConManMsg
case object ConManShutDown extends ConManMsg

object ConMan {
  val instance = new ConMan
  def props(): Props = {Props(instance)}
}

class ConMan extends Actor {
  import Tcp._
  import context.system
  val manager = IO(Tcp)

  override def preStart() {
    manager ! Bind(self, new InetSocketAddress("0.0.0.0", 51234))
  }
  def receive() = {
    case Bound(local) => {
      // Do stuff
    }
    case c @ Connected => {
      // Create Player Actor
    }
  }
}