package lostvaults.server
import akka.actor.{ Actor, ActorRef }

/**
 * The class room is a constructor for the rooms that are to be represented in the game. A room is composed of four boolean values and
 * four variables. The boolean values represent the possible ways for the player to take; north, east, south and west.
 * The two first variables represents the statuses of room; if it's created and if its connected to the "start room" or if its connected
 * to a room which is connected to the "start room".
 * The other two variables represent the list of player and the list of items in the room.
 */

class Room() {
  /*
   * Directions:
   * 0 = North
   * 1 = East
   * 2 = South
   * 3 = West
   */
  var exits: Array[Boolean] = Array[Boolean](false, false, false, false)
  var startRoom = false
  var created = false
  var connected = false
  var playerList: List[String] = List()
  var itemList: List[Item] = List()
  var NPCList: List[(String, ActorRef)] = List()
  var activeCombat: Option[ActorRef] = None 
  var roomDesc = ""

  /**
   * This method returns true if the direction exists in this room
   * @param direction the direction to check
   * @return true if this room has an exit in that direction, else false
   */
  def canMove(direction: Int) = {
    if (exits(direction))
      true
    else
      false
  }
  /**
   * This method add a player to the list of players in the room.
   * @param player The name of the player to be added to the room.
   */
  def addPlayer(player: String) = {
    playerList = player :: playerList
  }

  /**
   * This method removes a player from the list players in the room.
   * @param player The name of the player to be removed from the room.
   */
  def removePlayer(player: String) = {
    playerList = playerList.filterNot((c => c == player))
  }

  def getExitsString = {
    var s = ""
    if (exits(0)) {
      s = s + "North "
    }
    if (exits(1)) {
      s = s + "East "
    }
    if (exits(2)) {
      s = s + "South "
    }
    if (exits(3)) {
      s = s + "West "
    }
    if (startRoom) {
      s = s + "City"
    }
    s
  }
  
  def getExitsList = {
       var s: List[String] = List()
    if (exits(0)) {
      s = "North" :: s
    }
    if (exits(1)) {
      s = "East" :: s
    }
    if (exits(2)) {
      s = "South" :: s
    }
    if (exits(3)) {
      s = "West" :: s
    }
    s
  }
  /**
   * This method returns the list of players in a room.
   * @return the players in this room
   */
  def getPlayerList(): List[String] = {
    playerList
  }

  /**
   * This method checks if a given player is in the room.
   * @param player The name of the player to search for.
   * @return true if that player is in this room, else false
   */
  def hasPlayer(player: String): Boolean = {
    playerList.find(f => f.compareToIgnoreCase(player) == 0) != None
  }

  /**
   * This method adds an item to the list of items in a room.
   * @param newItem The name of the item to be added.
   */
  def addItem(newItem: Item) = {
    itemList = newItem :: itemList
  }

  /**
   * This method removes an item from the list of items in a room.
   * @param item The name of the Item to be removed.
   */
  def removeItem(item: Item) = {
    itemList = itemList.filterNot((c => c == item))
  }

  /**
   * This method returns the list of Items in a room.
   * @return the Items in the room
   */
  def getItemList(): List[Item] = {
    itemList
  }

  /**
   * Removes an Item from this Room and returns it.
   * @param the name of the Item
   */
  def takeItem(_name: String): Item = {
    val ret = itemList.find(i => i.name.compareToIgnoreCase(_name) == 0).get
    removeItem(ret)
    ret
  }

  /**
   * This method checks if a given item is in the room.
   * @param item The item to be examined
   * @return true if this room contains the item, else false
   */
  def hasItem(item: Item): Boolean = {
    itemList.contains(item)
  }

  /**
   * This method checks if an item with a given name is in the room.
   * @param item The name of the item to be examined
   * @return true if this room contains the item, else false
   */
  def hasItem(item: String): Boolean = {
    itemList.find(i => i.name.compareToIgnoreCase(item) == 0) != None
  }
  /**
   * This method adds an NPC to the list of NPCs in the room.
   * @param NPC the name of the NPC to be added.
   *
   */
  def addNPC(npc: (String, ActorRef)) = {
    NPCList = npc :: NPCList
  }

  /**
   * This method removes an NPC from the list of NPCs in the room.
   * @param NPC the name of the NPC to be removed.
   *
   */
  def removeNPC(npc: String) = {
    NPCList = NPCList.filterNot((c => c._1 == npc))
  }

  def getNPCString = {
    NPCList.foldRight("")((npc, list) => npc._1 + "\n" + list)
  }

  /**
   * This method checks if a given NPC is in the room.
   * @param NPC the name of the NPC to be searched for
   * @return true if this room has this NPC, else false
   */
  def hasNPC(npc: String): Boolean = {
    NPCList.exists(c => c._1.equalsIgnoreCase(npc))
  }
  def getNPCActorRef(npc: String): Option[ActorRef] = {
    var NPCTupleOption = NPCList.find(c => c._1.equalsIgnoreCase(npc))
    if (NPCTupleOption != None) {
      Some(NPCTupleOption.get._2)
    } else {
      None
    }
  }

/**
 * This method returns the Room's description.
 * @return the Room's description
 */
  def getDescription(name: String): String = {
    roomDesc
  }

  /**
   * Creates a description for this Room.
   */
  def createRoomDesc {
    roomDesc = RoomDescGen.generateDescription(NPCList.map(c => c._1), itemList.map(c => c.name), getExitsList)
    println(roomDesc)
  }
  
  /**
   * String representation of the room with the possible exits
   * marked by a N,E,S or W character for each direction
   * and a "*" if this room is a start room
   * @return string representation of the room
   */
  override def toString(): String = {
    "|" + (if (exits(3)) "W" else " ") + (if (exits(0)) "N" else " ") + (if (startRoom) "*" else " ") + (if (exits(2)) "S" else " ") + (if (exits(1)) "E" else " ")
  }
}
