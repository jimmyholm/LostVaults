package lostvaultsClient

import akka.actor.{ Actor, ActorRef, ActorSystem, Props, Terminated }
//import akka.pattern.ask
//import akka.util.Timeout
//import scala.concurrent.duration._
//import scala.concurrent.Await
//import java.util.concurrent.TimeUnit

object main {
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