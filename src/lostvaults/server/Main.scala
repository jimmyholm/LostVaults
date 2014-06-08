/**
 * Main.scala
 * @Author Felix Färsjö, Jimmy Holm, Fredrik Larsson, Anna Nilsson, Philip Åkerfeldt
 * @Version 1.0
 */

package lostvaults.server
import akka.actor.{ ActorSystem, ActorRef, Props, Inbox }
import scala.concurrent.duration._
import akka.util.Timeout
object Main {
  var system: Option[ActorSystem] = None
  var PMap: Option[ActorRef] = None
  var GMap: Option[ActorRef] = None
  var City: Option[ActorRef] = None
  var CheatActor: Option[ActorRef] = None

  def startUp() {
    system = Some(ActorSystem("LostVaultsServer"))
    ItemRepo.populateArray()
    val pmap = system.get.actorOf(Props[PlayerMap])
    PMap = Some(pmap) // Start up our player hashmap actor
    val gmap = system.get.actorOf(Props[GroupMap])
    GMap = Some(gmap)
    val conMan = system.get.actorOf(Props[ConMan])
    City = Some(system.get.actorOf(Dungeon.props(0)))
    City.get ! DungeonMakeCity
    CheatActor = Some(system.get.actorOf(Props[CheatActor]))
  }

  def restart() {
    system.get.shutdown()
    ItemRepo.clearArray()
    startUp
  }

  def main(args: Array[String]) {
    startUp
    var input = ""
    do {
      input = Console.readLine("Enter \"Quit\" to exit> ")
      if (input.compareToIgnoreCase("Quit") != 0) {
        CheatActor.get ! input
      }
    } while (input.toLowerCase() != "quit")
    system.get.shutdown()
  }
}
