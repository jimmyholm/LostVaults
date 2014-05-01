package lostvaults.server
import akka.actor.{ActorSystem, ActorRef, Props, Inbox}
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
object main {
  var PMap: Option[ActorRef] = None
  val system = ActorSystem("LostVaultsServer")
  var City: Option[ActorRef] = None
  def main(args: Array[String]) {
    val pmap = system.actorOf(Props[PlayerMap])
    PMap = Some(pmap) // Start up our player hashmap actor
    val conMan = system.actorOf(Props[ConMan])
    City = Some(system.actorOf(Props[Dungeon]))
    var input = ""
    City.get ! DungeonMakeCity
    do {
      input = Console.readLine("Enter \"Quit\" to exit> ")
    } while (input.toLowerCase() != "quit")
    system.shutdown()
  }
}