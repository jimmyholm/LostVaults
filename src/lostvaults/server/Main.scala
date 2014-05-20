package lostvaults.server
import akka.actor.{ActorSystem, ActorRef, Props, Inbox}
import scala.concurrent.duration._
import akka.util.Timeout
object Main {
  var PMap: Option[ActorRef] = None
  var GMap: Option[ActorRef] = None
  val system = ActorSystem("LostVaultsServer")
  var City: Option[ActorRef] = None
  def main(args: Array[String]) {
    ItemRepo.populateArray()
    println(ItemRepo.getById(1).compareTo(3))
    val pmap = system.actorOf(Props[PlayerMap])
    PMap = Some(pmap) // Start up our player hashmap actor
    val gmap = system.actorOf(Props[GroupMap])
    GMap = Some(gmap)
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