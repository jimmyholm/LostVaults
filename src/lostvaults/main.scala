package lostvaults
// Server main file
import akka.actor.{ ActorSystem, Actor, ActorRef, Props }
object main {
  var PMap: Option[ActorRef] = None
  val system = ActorSystem("LostVaultsServer")
  def main(args: Array[String]) {
    PMap = Some(system.actorOf(Props[PlayerMap])) // Start up our player hashmap actor
    val conMan = system.actorOf(Props[ConMan])
    val city = system.actorOf(Props[Dungeon])
    var input = ""
    city ! DungeonMakeCity
    do {
      input = Console.readLine("Enter \"Quit\" to exit> ")
    } while (input.toLowerCase() != "quit")
    system.shutdown()
  }
}