package lostvaults.server

import akka.actor.Actor
import scala.collection.mutable.Set
import lostvaults.Parser
/**
 * Special case message which tells a dungeon to act as the city process. Only sent
 * to a single dungeon actor instance at the start of the server's life.
 */
case object DungeonMakeCity
/**
 * Dungeon is an actor class in charge of carrying the Main logic of the game,
 *  either as a dungeon or as the special-case City actor. The dungeon allows for
 *  interaction between players, such as chatting etc, while making sure that
 *  players who are not in the same logical space cannot interact with each other.
 */
class Dungeon extends Actor {
  import context.become
  import scala.collection.mutable.Set
  var PSet: Set[String] = Set()
  val PMap = Main.PMap.get
  def receive() = {
    case DungeonMakeCity => {
      become(CityReceive)
    }
  }

  def CityReceive: Receive = {
    case GameSay(name, msg) => {
      PSet foreach (c =>
        PMap ! PMapGetPlayer(c, "Say " + name + " " + msg))
    }
    case GameAddPlayer(name) => {
      PSet foreach (c => PMap ! PMapGetPlayer(c, "AddPlayer " + name)) // Send "GamePlayerEnter" to all other players*/
      PSet += name
      PMap ! PMapGetPlayer(name, "MovePlayer " + name)
    }
    case GameRemovePlayer(name) => {
      PSet -= name
      PSet foreach (c => if (name != c) PMap ! PMapGetPlayer(c, "RemovePlayer " + name)) // Send "GamePlayerEnter" to all other players*/ 
    }
    case PMapGetPlayerResponse(player, purpose) => {
      val action = Parser.findWord(purpose, 0)
      action match {
        case "Say" => {
          val name = Parser.findWord(purpose, 1)
          val msg = Parser.findRest(purpose, 1)
          if (!player.isEmpty)
            player.get ! GameSay(name, msg)
        }
        case "AddPlayer" => {
          val name = Parser.findWord(purpose, 1)
          if (!player.isEmpty)
            player.get ! GamePlayerEnter(name)
        }
        case "RemovePlayer" => {
          val name = Parser.findWord(purpose, 1)
          if (!player.isEmpty)
            player.get ! GamePlayerLeft(name)
        }
        case "MovePlayer" => {
          val name = Parser.findWord(purpose, 1)
          if (!player.isEmpty)
            player.get ! GameMoveToDungeon(self)
        }
      }
    }
  }

}