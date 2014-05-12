package lostvaults.server

import akka.actor.{ Actor, Props, ActorRef }
import scala.collection.mutable.Set
import lostvaults.Parser
/**
 * Special case message which tells a dungeon to act as the city process. Only sent
 * to a single dungeon actor instance at the start of the server's life.
 */
case object DungeonMakeCity

case object NewDungeon
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
  var activeCombat: Option[ActorRef] = None

  def receive() = {
    case DungeonMakeCity => {
      //val CityRoom = new Room()
      // lägg till alla NPCs
      become(CityReceive)
    }
    case NewDungeon => {
      val Rooms = new Array[Room](100) // 10 x 10 Room array
      RoomRandom.init(Rooms)
      //become(DungeonReceive)
    }
  }

  def CityReceive: Receive = {
    case GameAttackPlayer(attacker, attackee) => {
      if (PSet.contains(attackee)) {
        println(attacker + " attacks " + attackee)
        if (activeCombat == None) {
          println("New combat actor created.")
          activeCombat = Some(context.actorOf(Props[Combat]))
          activeCombat.get ! self
        }
        println("Adding " + attacker + " to combat")
        PMap ! PMapSendGameMessage(attacker, GamePlayerJoinBattle(activeCombat.get))
        println("Adding " + attackee + " to combat")
        PMap ! PMapSendGameMessage(attackee, GamePlayerJoinBattle(activeCombat.get))
      } else {
    	  PMap ! PMapSendGameMessage(attacker, GameAttackNotInRoom(attackee))
      }
    }
    case GameSay(name, msg) => {
      PSet foreach (c =>
        PMap ! PMapSendGameMessage(c, GameSay(name, msg)))
    }

    case GameAddPlayer(name) => {
      PSet foreach (c => if (name != c) PMap ! PMapSendGameMessage(c, GamePlayerEnter(name)))
      PSet += name
      PMap ! PMapSendGameMessage(name, GameMoveToDungeon(self))
    }

    case GameRemovePlayer(name) => {
      PSet -= name
      PSet foreach (c => if (name != c) PMap ! PMapSendGameMessage(c, GamePlayerLeft(name))) // Send "GamePlayerLeft" to all other players*/ 
    }

    case GameNotifyDungeon(msg) => {
      PSet foreach (c => (PMap ! PMapSendGameMessage(c, GameSystem(msg))))
    }
    case GameNotifyRoom(msg) => {
      // Find all players in room 
    }

    case PMapFailure => {
      println("DUNGEON: Failed to send a player a message.\nEnter \"Quit\" to exit > ")
    }
  }
}