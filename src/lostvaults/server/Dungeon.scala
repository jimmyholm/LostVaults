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
  val GMap = Main.GMap.get
  var activeCombat: Option[ActorRef] = None
  var rooms: Array[Room] = Array()
  var entrance: (Int, Int) = (-1, -1)
  val gen = new RoomGenerator

  def indexToCoords(index: Int): (Int, Int) = {
    (index % gen.Width, index / gen.Width)
  }

  def findRoom(name: String): Int = {
    if (PSet.find(n => n == name) == None)
      -1
    var i = 0
    for (i <- 0 until (gen.Width * gen.Height)) {
      if (rooms(i).hasPlayer(name))
        i
    }
    -1
  }

  def receive() = {
    case DungeonMakeCity => {
      //val CityRoom = new Room()
      //lägg till alla NPCs
      become(CityReceive)
    }
    case NewDungeon => {
      entrance = gen.startRoom
      rooms = gen.generateDungeon()
      println("New dungeon generated!")
    }
    case GameSay(name, msg) => {
      val roomNum = findRoom(name)
      if (roomNum == -1)
        sender ! GameSystem("The player who tried to speak is not in this dungeon! The world is crumbling!")
      val players = rooms(roomNum).getPlayerList
      players.foreach(n => PMap ! PMapSendGameMessage(name, GameSay(name, msg)))
    }

    case GameAddPlayer(name) => {
      println("Adding player " + name)
      PSet foreach (c => if (name != c) PMap ! PMapSendGameMessage(c, GamePlayerEnter(name)))
      var PString = ""
      PSet foreach (c => PString = c + "\n" + PString)
      PSet += name
      PMap ! PMapSendGameMessage(name, GameMoveToDungeon(self))
      PMap ! PMapSendGameMessage(name, GameMessage("DUNGEONLIST " + PString))
      rooms(gen.coordToIndex(entrance)).addPlayer(name)
      PMap ! PMapSendGameMessage(name, GameDungeonMove(entrance, true))
      PMap ! PMapSendGameMessage(name, GameSystem(rooms(gen.coordToIndex(entrance)).getDescription(name)))
    }

    case GamePlayerMove(name, dir) => {
      println("Moving player " + name)
      val room = findRoom(name)
      val coord = indexToCoords(room)
      if (rooms(room).canMove(dir)) {
        println("Can move player.")
        val move = 
          dir match {
          case 0 => (coord._1, coord._2-1)
          case 1 => (coord._1+1, coord._2)
          case 2 => (coord._1, coord._2)
          case 3 => (coord._1-1, coord._2)
        }
        val nextRoom = gen.coordToIndex(move)
        rooms(room).removePlayer(name)
        val pListOld = rooms(room).getPlayerList()
        val pListNew = rooms(nextRoom).getPlayerList()
        var dirStr =
          dir match {
            case 0 => "Northern"
            case 1 => "Eastern"
            case 2 => "Southern"
            case _ => "Western"
          }
        pListOld.foreach(n => PMap ! PMapSendGameMessage(n, GameSystem("Player " + name + " left through the " + dirStr + " exit.")))
        dirStr =
          dir match {
            case 0 => "Southern"
            case 1 => "Western"
            case 2 => "Northern"
            case _ => "Eastern"
          }
        pListNew.foreach(n => PMap ! PMapSendGameMessage(n, GameSystem("Player " + name + " entered through the " + dirStr + " entrance.")))
        rooms(nextRoom).addPlayer(name)
        PMap ! PMapSendGameMessage(name, GameDungeonMove(move, false))
        PMap ! PMapSendGameMessage(name, GameSystem(rooms(nextRoom).getDescription(name)))
      } else {
        println("Cannot move player.")
        PMap ! PMapSendGameMessage(name, GameSystem("You cannot move in that direction."))
      }
    }
    case GameExitDungeon(name) => {
      val room = findRoom(name)
      if (room != entrance)
        PMap ! PMapSendGameMessage(name, GameSystem("You can only leave the dungeon from the dungeon exit."))
      else {
    	self ! GameRemovePlayer(name)
      }
    }
    case GameRemovePlayer(name) => {
      PSet -= name
      PSet foreach (c => if (name != c) PMap ! PMapSendGameMessage(c, GamePlayerLeft(name))) // Send "GamePlayerLeft" to all other players
      Main.City .get ! GameAddPlayer(name)
      PMap ! PMapSendGameMessage(name, GameMoveToDungeon(Main.City.get))
      GMap ! GMapExitDungeon(name)
      if (PSet isEmpty) {
        // Give quest rewards.
        context stop self
      }
    }

    case GameNotifyDungeon(msg) => {
      PSet foreach (c => (PMap ! PMapSendGameMessage(c, GameSystem(msg))))
    }
    case GameNotifyRoom(name, msg) => {
      val room = findRoom(name)
      if(name != (-1,-1))
        rooms(room).getPlayerList().foreach(n => PMap ! PMapSendGameMessage(n, GameSystem(msg)))
    }
  }

  def CityReceive: Receive = {
    case GameAttackPlayer(attacker, attackee) => {
      println("PSet: " + PSet + " attackee: " + attackee)
      if (PSet.contains(attackee)) {
        println(attacker + " attacks " + attackee)
        if (activeCombat == None) {
          println("New combat actor created.")
          activeCombat = Some(context.actorOf(Props[Combat]))
          activeCombat.get ! self
        }
        println("Adding " + attacker + " to combat")
        PMap ! PMapSendGameMessage(attacker, GamePlayerJoinBattle(activeCombat.get, attackee))
        PMap ! PMapSendGameMessage(attacker, GameMessage("You have attacked " + attackee))
        println("Adding " + attackee + " to combat")
        PMap ! PMapSendGameMessage(attackee, GamePlayerJoinBattle(activeCombat.get, attacker))
        PMap ! PMapSendGameMessage(attackee, GameMessage("You have been attacked by " + attacker))
      } else {
        PMap ! PMapSendGameMessage(attacker, GameAttackNotInRoom(attackee))
      }
    }
    case GameAttackPlayerInCombat(attackee) => {
      if (PSet contains(attackee)) {
        sender() ! GameYourTurn
      } else {
        sender() ! GameMessage("The player you are trying to attack is not in the game")
      }
    }
      
    case GameCombatFinished => {
      activeCombat = None
    }
    
    case GameSay(name, msg) => {
      PSet foreach (c =>
        PMap ! PMapSendGameMessage(c, GameSay(name, msg)))
    }

    case GameAddPlayer(name) => {
      PSet foreach (c => if (name != c) PMap ! PMapSendGameMessage(c, GamePlayerEnter(name)))
      var PString = ""
      PSet foreach (c => PString = c + "\n" + PString)
      PSet += name
      PMap ! PMapSendGameMessage(name, GameMoveToDungeon(self))
      PMap ! PMapSendGameMessage(name, GameMessage("DUNGEONLIST " + PString))
    }

    case GameRemovePlayer(name) => {
      PSet -= name
      PSet foreach (c => if (name != c) PMap ! PMapSendGameMessage(c, GamePlayerLeft(name))) // Send "GamePlayerLeft" to all other players*/
      println("PSet after remove of " + name + ": " + PSet)
    }

    case GameNotifyDungeon(msg) => {
      PSet foreach (c => (PMap ! PMapSendGameMessage(c, GameSystem(msg))))
    }
    case GameNotifyRoom(name, msg) => {
      // Find all players in room 
    }
    case GameEnterDungeon(name) => {
      GMap ! GMapEnterDungeon(name)
    }
    case PMapFailure => {
      println("DUNGEON: Failed to send a player a message.\nEnter \"Quit\" to exit > ")
    }
  }
}