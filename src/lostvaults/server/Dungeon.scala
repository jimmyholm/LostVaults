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

object Dungeon {
  def props(id: Int): Props = Props(new Dungeon(id))
}

/**
 * Dungeon is an actor class in charge of carrying the Main logic of the game,
 *  either as a dungeon or as the special-case City actor. The dungeon allows for
 *  interaction between players, such as chatting etc, while making sure that
 *  players who are not in the same logical space cannot interact with each other.
 */
class Dungeon(id: Int) extends Actor {
  import context.become
  import scala.collection.mutable.Set
  var PSet: Set[String] = Set()
  val PMap = Main.PMap.get
  val GMap = Main.GMap.get
  var rooms: Array[Room] = Array()
  var entrance: Int = 0
  val gen = new RoomGenerator
  var myID = id
  var nextID = id + 1
  var nextCombat = 1
  def indexToCoords(index: Int): (Int, Int) = {
    (index % gen.Width, index / gen.Width)
  }

  def compareStrings(str1: String, str2: String) = {
    var matches = 0
    var leng = 0
    if (str1.length > str2.length) {
      leng = str1.length
      for (i <- 0 until str2.length) {
        if (str1(i).toString().compareToIgnoreCase(str2(i).toString()) == 0 || (str1(str1.length - 1 - i).toString().compareToIgnoreCase(str2(str2.length - 1 - i).toString()) == 0)) {
          matches += 1
        }
      }
    } else {
      leng = str2.length
      for (i <- 0 until str1.length) {
        if (str1(i).toString().compareToIgnoreCase(str2(i).toString()) == 0 || (str1(str1.length - 1 - i).toString().compareToIgnoreCase(str2(str2.length - 1 - i).toString()) == 0)) {
          matches += 1
        }
      }
      if (matches == 0) // Check the reverse as well.
        for (i <- (str1.length - 1) to 0 by -1)
          if (str1(i).toString().compareToIgnoreCase(str2(i).toString()) == 0) {
            matches += 1
          }
    }
    matches.asInstanceOf[Float] / leng.asInstanceOf[Float]
  }

  def findRoom(name: String): Int = {
    if (PSet.find(n => n.compareToIgnoreCase(name) == 0) == None)
      -1
    else {
      for (i <- 0 until (gen.Width * gen.Height)) {
        if (rooms(i).hasPlayer(name))
          return i
      }
      -1
    }
  }

  def receive() = {
    case DungeonMakeCity => {
      become(CityReceive)
    }
    case NewDungeon => {
      entrance = gen.coordToIndex(gen.startRoom)
      rooms = gen.generateDungeon(context.system, self)
      println("New dungeon (ID: " + myID + ") generated!")
    }
    case GameSay(name, msg) => {
      val roomNum = findRoom(name)
      if (roomNum == -1) {
        sender ! GameSystem("The player who tried to speak is not in this dungeon! The world is crumbling!")
      } else {
        val players = rooms(roomNum).getPlayerList
        players.foreach(n => PMap ! PMapSendGameMessage(n, GameSay(name, msg)))
      }
    }

    case GMapJoin(_, _) => {
      sender ! GameMessage("You may not join a new group when in a dungeon!")
    }

    case GMapLeave(_) => {
      sender ! GameMessage("You may not leave a group when in a dungeon!")
    }

    case GameAddPlayer(name) => {
      println("(Dungeon: " + myID + ") Adding player " + name)
      PSet foreach (c => if (name.compareToIgnoreCase(c) != 0) PMap ! PMapSendGameMessage(c, GamePlayerEnter(name)))
      var PString = ""
      PSet foreach (c => PString = c + "\n" + PString)
      PSet += name
      PMap ! PMapSendGameMessage(name, GameMoveToDungeon(self))
      PMap ! PMapSendGameMessage(name, GameMessage("DUNGEONLIST " + PString))
      rooms(entrance).addPlayer(name)
      PMap ! PMapSendGameMessage(name, GameDungeonMove(entrance, true))
      PMap ! PMapSendGameMessage(name, GameSystem(rooms(entrance).getDescription(name)))
      PMap ! PMapSendGameMessage(name, GameMessage("ROOMEXITS " + rooms(entrance).getExitsString))
    }

    case GamePlayerMove(name, dir, index) => {
      println("(Dungeon: " + myID + ") Moving player " + name)
      //val room = findRoom(name)
      rooms(index).getPlayerList.foreach(c => if (c != name) { PMap ! PMapSendGameMessage(c, GameMessage("ROOMLEFT " + name)) })
      val coord = indexToCoords(index)
      if (rooms(index).canMove(dir)) {
        val move =
          dir match {
            case 0 => (coord._1, coord._2 - 1)
            case 1 => (coord._1 + 1, coord._2)
            case 2 => (coord._1, coord._2 + 1)
            case 3 => (coord._1 - 1, coord._2)
          }
        val nextRoom = gen.coordToIndex(move)
        rooms(index).removePlayer(name)
        val pListOld = rooms(index).getPlayerList()
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
        PMap ! PMapSendGameMessage(name, GameDungeonMove(nextRoom, false))
        PMap ! PMapSendGameMessage(name, GameMessage("ROOMEXITS " + rooms(nextRoom).getExitsString))
        PMap ! PMapSendGameMessage(name, GameMessage("NPCLIST " + rooms(nextRoom).getNPCString))
        PMap ! PMapSendGameMessage(name, GameSystem(rooms(nextRoom).getDescription(name)))
        rooms(nextRoom).getPlayerList.foreach(c => if (c != name) { PMap ! PMapSendGameMessage(c, GameMessage("ROOMJOIN " + name)) })
        PMap ! PMapSendGameMessage(name, GameMessage("ROOMLIST " + rooms(nextRoom).getPlayerList.foldRight("")((pName, s) => if (pName != name) { pName + "\n" + s } else { "" + s })))
        if (rooms(nextRoom).getItemList.isEmpty) {
          PMap ! PMapSendGameMessage(name, GameMessage("ITEMLIST  "))
        } else {
          var retString = ""
          rooms(nextRoom).getItemList.foreach(c => (retString += c.name + "\n"))
          PMap ! PMapSendGameMessage(name, GameMessage("ITEMLIST " + retString))
        }
      } else {
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
      Main.City.get ! GameAddPlayer(name)
      PMap ! PMapSendGameMessage(name, GameMessage("GUICITY"))
      PMap ! PMapSendGameMessage(name, GameMoveToDungeon(Main.City.get))
      GMap ! GMapExitDungeon(name)
      if (PSet isEmpty) {
        // Give quest rewards.
        context stop self
      }
    }
    case GameRemoveNPCFromRoom(npc, room) => {
      rooms(room).removeNPC(npc)
      rooms(room).playerList foreach (c => PMap ! PMapSendGameMessage(c, GameMessage("NPCLEFT " + npc)))
    }

    case GameAttackPlayer(attacker, attackee) => {
      var nameMatches: List[(Float, String)] = List()
      var ind = ""
      PSet foreach (name => {
        if (name.compareToIgnoreCase(attacker) != 0) {
          nameMatches = (compareStrings(attackee, name), name) :: nameMatches
        }
      })
      if (nameMatches isEmpty) { // Not a player, check NPCs.
        val npcs = rooms(findRoom(attacker)).NPCList
        npcs foreach (name => {
          nameMatches = (compareStrings(attackee, name._1), name._1) :: nameMatches
        })
        if (nameMatches isEmpty)
          PMap ! PMapSendGameMessage(attacker, GameMessage("There is no one with that name to attack."))
      }
      if (!nameMatches.isEmpty) {
        var highest = -1.0
        nameMatches foreach (it => { if (it._1 > highest && it._1 >= 0.25) { highest = it._1; ind = it._2 } })
        if (ind == "") {
          PMap ! PMapSendGameMessage(attacker, GameMessage("There is no one with that name to attack."))
        } else {
          var attackee = ind
          var currentRoom = findRoom(attacker)
          if (rooms(currentRoom).hasPlayer(attackee)) {
            println("(Dungeon: " + myID + ") " + attacker + " attacks player " + attackee)
            if (rooms(currentRoom).activeCombat == None) {
              println("(Dungeon: " + myID + ") New combat actor created - attackee == Player")
              rooms(currentRoom).activeCombat = Some(context.actorOf(Combat.props(self, currentRoom, myID, nextCombat)))
              rooms(currentRoom).activeCombat.get ! self
              nextCombat += 1
            }
            println("(Dungeon: " + myID + ") Adding " + attackee + " to combat")
            PMap ! PMapSendGameMessage(attackee, GamePlayerJoinBattle(rooms(currentRoom).activeCombat.get, attacker))
            PMap ! PMapSendGameMessage(attackee, GameMessage("You have been attacked by " + attacker))
            println("(Dungeon: " + myID + ") Adding " + attacker + " to combat")
            PMap ! PMapSendGameMessage(attacker, GamePlayerJoinBattle(rooms(currentRoom).activeCombat.get, attackee))
            PMap ! PMapSendGameMessage(attacker, GameMessage("You have attacked " + attackee))
          } else if (rooms(currentRoom).hasNPC(attackee)) {
            println("(Dungeon: " + myID + ") " + attacker + " attacks player " + attackee)
            if (rooms(currentRoom).activeCombat == None) {
              println("(Dungeon: " + myID + ") New combat actor created - attackee == Monster")
              rooms(currentRoom).activeCombat = Some(context.actorOf(Combat.props(self, currentRoom, myID, nextCombat)))
              rooms(currentRoom).activeCombat.get ! self
            }
            println("(Dungeon: " + myID + ") Adding " + attackee + " to combat")
            var npc = rooms(currentRoom).getNPCActorRef(attackee)
            if (npc != None) {
              npc.get ! GamePlayerJoinBattle(rooms(currentRoom).activeCombat.get, attacker)
            }
            println("(Dungeon: " + myID + ") Adding " + attacker + " to combat")
            PMap ! PMapSendGameMessage(attacker, GamePlayerJoinBattle(rooms(currentRoom).activeCombat.get, attackee))
            PMap ! PMapSendGameMessage(attacker, GameMessage("You have attacked " + attackee))
            PMap ! PMapSendGameMessage(attacker, GamePlayerSetTarget(attackee))
          } else {
            PMap ! PMapSendGameMessage(attacker, GameAttackNotInRoom(attackee))
          }
        }
      }
    }
    case GameAttackPlayerInCombat(attackee) => {
      if (PSet contains (attackee)) {
        sender() ! GameYourTurn
      } else {
        sender() ! GameMessage("The player you are trying to attack is not in the game")
      }
    }
    case GameCombatFinished(room: Int) => {
      rooms(room).activeCombat = None
    }
    case GameNotifyDungeon(msg) => {
      PSet foreach (c => (PMap ! PMapSendGameMessage(c, GameSystem(msg))))
    }
    case GameNotifyRoomByName(name, msg) => {
      val room = findRoom(name)
      if (room != -1)
        rooms(room).getPlayerList().foreach(n => PMap ! PMapSendGameMessage(n, GameSystem(msg)))
    }
    case GameNotifyRoom(room, msg) => {
      if (rooms(room) != -1)
        rooms(room).getPlayerList().foreach(n => (PMap ! PMapSendGameMessage(n, GameSystem(msg))))
    }

    case GamePickUpItem(item, currentWep, currentArmor, name, index) => {
      // Grab all items in room.
      val items = rooms(index).getItemList
      var itemMatch: List[(Float, Int)] = List()
      var i = 0
      if (items.isEmpty) {
        PMap ! PMapSendGameMessage(name, GameMessage("There are no items in this rooms to pick up."))
      } else {
        items foreach (it => { itemMatch = (compareStrings(item, it.name), i) :: itemMatch; i += 1 })
        var highest = -1.0
        var ind = -1
        itemMatch foreach (it => { if (it._1 > highest && it._1 >= 0.5) { highest = it._1; ind = it._2 }; })
        if (ind == -1) {
          PMap ! PMapSendGameMessage(name, GameMessage("There's no item with that name in this room."))
        } else {
          val pItem = rooms(index).takeItem(items(ind).name)
          if (pItem.isWeapon) {
            rooms(index).addItem(ItemRepo.getById(currentWep))
            rooms(index).getPlayerList().foreach(n =>
              (PMap ! PMapSendGameMessage(n, GameMessage("ITEMJOIN " + ItemRepo.getById(currentWep).name))))

          } else if (pItem.isArmor) {
            rooms(index).addItem(ItemRepo.getById(currentArmor))
            rooms(index).getPlayerList().foreach(n =>
              (PMap ! PMapSendGameMessage(n, GameMessage("ITEMJOIN " + ItemRepo.getById(currentArmor).name))))

          }
          PMap ! PMapSendGameMessage(name, GameUpdateItem(pItem))
          var msg = "ITEMLEFT " + pItem.name
          rooms(index).getPlayerList().foreach(n => (PMap ! PMapSendGameMessage(n, GameMessage(msg))))
        }
      }

      // Find nearest item string.

      /*if (rooms(index).hasItem(item)) {
        val pItem = rooms(index).takeItem(item)
        if (pItem.isWeapon) {
          rooms(index).addItem(ItemRepo.getById(currentWep))
        } else if (pItem.isArmor) {
          rooms(index).addItem(ItemRepo.getById(currentArmor))
        }
        PMap ! PMapSendGameMessage(name, GameUpdateItem(pItem))
        var msg = "ITEMLEFT " + pItem.name
        println(msg+"--------------------------------")
        rooms(index).getPlayerList().foreach(n => (PMap ! PMapSendGameMessage(n, GameMessage(msg))))
      } else {
        PMap ! PMapSendGameMessage(name, GameMessage("No such item in the room."))
      }*/
    }

    case GameDropItem(item, roomIndex) => {
      var msg = "ITEMJOIN " + item.name
      rooms(roomIndex).addItem(item)
      if (rooms(roomIndex) != -1) {
        rooms(roomIndex).getPlayerList().foreach(n => (PMap ! PMapSendGameMessage(n, GameMessage(msg))))
      }
    }

  }

  def CityReceive: Receive = {
    case GMapJoin(joinee, group) => {
      println("(City) Player " + joinee + " wants to join " + group + "'s group.")
      GMap ! GMapJoin(joinee, group)
    }
    case GMapLeave(name) => {
      println("(City) Player " + name + " wants to leave their group.")
      GMap ! GMapLeave(name)
    }
    case GameSay(name, msg) => {
      PSet foreach (c =>
        PMap ! PMapSendGameMessage(c, GameSay(name, msg)))
    }

    case GameAddPlayer(name) => {
      println("(City) Player " + name + " joined the City.")
      PSet foreach (c => if (name != c) PMap ! PMapSendGameMessage(c, GamePlayerEnter(name)))
      var PString = ""
      PSet foreach (c => PString = c + "\n" + PString)
      PSet += name
      PMap ! PMapSendGameMessage(name, GameMoveToDungeon(self))
      PMap ! PMapSendGameMessage(name, GameMessage("DUNGEONLIST " + PString))
    }

    case GameRemovePlayer(name) => {
      println("(City) Player " + name + " left the City.")
      PSet -= name
      PSet foreach (c => if (name != c) PMap ! PMapSendGameMessage(c, GamePlayerLeft(name))) // Send "GamePlayerLeft" to all other players*/
    }

    case GameNotifyDungeon(msg) => {
      PSet foreach (c => (PMap ! PMapSendGameMessage(c, GameSystem(msg))))
    }
    case GameNotifyRoomByName(name, msg) => {
      PSet foreach (c => (PMap ! PMapSendGameMessage(c, GameSystem(msg))))
    }
    case GameEnterDungeon(name) => {
      GMap ! GMapEnterDungeon(name)
    }
    case PMapFailure => {
      println("(City) Failed to send a player a message.")
    }
  }
}
