package lostvaults.client
import akka.actor.{ Actor, ActorRef, ActorSystem, Props, Terminated }

object Main {
  val system = ActorSystem("GameClient")
  var Running = true

  object Watcher {
    def props(toWatch: ActorRef): Props = { Props(new Watcher(toWatch)) }
  }
  class Watcher(toWatch: ActorRef) extends Actor {
    override def preStart() = { context.watch(toWatch) }
    def receive() = {
      case Terminated(actor) =>
        Running = false
    }
  }
  
  
  def main(args: Array[String]) {
	val game = system.actorOf(playGame.props)
	val watcher = system.actorOf(Watcher.props(game))
    while(Running){

    }
	system.shutdown()
  }

}
