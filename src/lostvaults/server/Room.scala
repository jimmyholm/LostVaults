package lostvaults.server

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
  //  var NPCList: List[NPC]

  def canMove(direction: Int) = {
    if (exits(direction))
      true
    else
      false
  }
  /**
   * This method add a player to the list of players in the room.
   * @param player The name of the player to be added to the room.
   *
   */
  def addPlayer(player: String) = {
    player :: playerList
  }

  /**
   * This method removes a player from the list players in the room.
   * @param player The name of the player to be removed from the room.
   */
  def removePlayer(player: String) = {
    playerList = playerList.filterNot((c => c == player))
  }

  /**
   * This method returns the list of players in a room.
   *
   */
  def getPlayerList(): List[String] = {
    return playerList
  }

  /**
   * This method checks if a given player is in the room.
   * @param player The name of the player to search for.
   */
  def hasPlayer(player: String): Boolean = {
    playerList.find(f => f == player) != None
  }

  /**
   * This method adds an item to the list of items in a room.
   * @param newItem The name of the item to be added.
   */
  def addItem(newItem: Item) = {
    newItem :: itemList
  }

  /**
   * This method removes an item from the list of items in a room.
   * @param item The name of the item to be removed.
   */
  def removeItem(item: Item) = {
    itemList = itemList.filterNot((c => c == item))
  }

  /**
   * This method returns the list of items in a room.
   *
   */
  def getItemList(): List[Item] = {
    return itemList
  }

  /**
   * This method checks if a given item is in the room.
   * @param item The name of the item to be searched for.
   */
  def hasItem(item: Item): Boolean = {
    return itemList.contains(item)
  }
  /**
   * This method adds an NPC to the list of NPCs in the room.
   * @param NPC The name of the NPC to be added.
   *
   */
  //  def addNPC(NPC: NPC) = {
  //    NPC::NPCList
  //  }
  //

  /**
   * This method removes an NPC from the list of NPCs in the room.
   * @param NPC The name of the NPC to be removed.
   *
   */
  //  def removeNPC(NPC: NPC) = {
  //    NPCList = NPCList.filterNot((c => c == NPC))
  //  }
  //

  /**
   * This method returns the list of NPCs in the room.
   *
   */
  //  def getNPCList() : List[NPC] = {
  //    return NPCList
  //  }
  //

  /**
   * This method checks if a given NPC is in the room.
   * @param NPC The name of the NPC to be searched for.
   *
   */
  //  def hasNPC(NPC: NPC) : Boolean = {
  //    return NPCList.contains(nPC) 
  //  }

  def getDescription(name: String): String = {
    var ret = "You are standing in a wide, open room.\n"
    ret += "You see exits to the:" + "\n"
    if (exits(0))
      ret += "North\n"
    if (exits(1))
      ret += "East\n"
    if (exits(2))
      ret += "South\n"
    if (exits(3))
      ret += "West\n"
    if (startRoom)
      ret += "You also see an exit to the city above.\n"
    if (!itemList.isEmpty) {
      ret += "On the floor you find: \n"
      itemList.foreach(item => ret += item.name + "\n")
    }
    if (playerList.size > 1) {
      ret += "Also in the room are: \n"
      playerList.foreach(o => if (o != name) ret += name + "\n")
    }
    ret
  }

  override def toString(): String = {
    "|" + (if (exits(3)) "W" else " ") + (if (exits(0)) "N" else " ") + (if (startRoom) "*" else " ") + (if (exits(2)) "S" else " ") + (if (exits(1)) "E" else " ")
  }
}
