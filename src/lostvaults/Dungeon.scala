package lostvaults
import akka.actor.{ Actor, ActorRef, Props }
case object DungeonMakeCity

class Dungeon extends Actor {
  import context.become
  import scala.collection.mutable.Set
  var PSet: Set[String] = Set()
  val PMap = main.PMap.get
  def receive() = {
    case DungeonMakeCity => {
      become(CityReceive)
    }
  }

  def CityReceive: Receive = {
    case GameSay(name, msg) => {
      PSet foreach (c =>
        if (c != name) {
          PMap ! PMapGetPlayer(c, "Say " + name + msg)
        })
    }
    case GameAddPlayer(name) => {
      
      PSet foreach (c => PMap ! PMapGetPlayer(c, "AddPlayer " + name)) // Send "GamePlayerEnter" to all other players*/
      PSet += name
    }
    case GameRemovePlayer(name) => {
      PSet -= name
      PSet foreach (c => PMap ! PMapGetPlayer(c, "RemovePlayer " + name) ) // Send "GamePlayerEnter" to all other players*/ 
    }
    case PMapGetPlayerResponse(player, purpose) => {
    	val action = Parser.FindWord(purpose, 0)
    	action match {
    	  case "Say" => {
    	    val name = Parser.FindWord(purpose, 1)
    	    val msg = Parser.FindRest(purpose, 1)
    	    if(!player.isEmpty)
    	    	player.get ! GameSay(name, msg) 
    	  }
    	  case "AddPlayer" => {
    	    val name = Parser.FindWord(purpose, 1) 
    	    if(!player.isEmpty)
    	    	player.get ! GamePlayerEnter(name)
    	  }
    	  case "RemovePlayer" => {
    	    val name = Parser.FindWord(purpose, 1) 
    	    if(!player.isEmpty)
    	    	player.get ! GamePlayerLeft(name)
    	  }
    	}
    }
  }

}