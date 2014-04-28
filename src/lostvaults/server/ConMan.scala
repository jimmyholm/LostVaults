package lostvaults.server
import akka.actor.{Actor, Props}
import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress
sealed trait ConManMsg
case object ConManShutDown extends ConManMsg

/** Connection Manager class used to monitor incoming connections
 *  which are then matched up with newly-spawned player actors.
 *  
 *  The Connection manager performs no other function than to act as an entry
 *  point for new connections.
 */
class ConMan extends Actor {
  import Tcp._
  import context.system
  val manager = IO(Tcp)

  override def preStart() {
    manager ! Bind(self, new InetSocketAddress("0.0.0.0", 51234))
  }
  def receive() = {
    case CommandFailed(_: Bind) => {
      println("Failed to bind to 0.0.0.0:51234")
    }
    case Bound(local) => {
      // Do interesting stuff
    }
    case c @ Connected(remote, local) => {
      val newplayer = context.actorOf(Props[Player])
      sender ! Register(newplayer)
      println("New connection from: " + remote)
    }
  }
}