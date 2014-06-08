/**
 * CheatActor.scala
 * @Author Felix Färsjö, Jimmy Holm, Fredrik Larsson, Anna Nilsson, Philip Åkerfeldt
 * @Version 1.0
 */
package lostvaults.server
import akka.actor.{Actor, Props}
import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress
sealed trait ConManMsg
case object ConManShutDown extends ConManMsg

/** Connection Manager class is used to monitor incoming connections
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
    	println("Bound to port " + local.getPort)
    }
    case c @ Connected(remote, local) => {
      println("ConnectionManager: New connection from " + remote.getHostName())
      val newplayer = context.actorOf(Props[Player])
      sender ! Register(newplayer)
    }
  }
}
