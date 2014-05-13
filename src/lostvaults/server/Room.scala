package lostvaults.server

/**
 * The class room is a constructor for the rooms that are to be represented in the game. A room is composed of four boolean values and
 * four variables. The boolean values represent the possible ways for the player to take; north, east, south and west.
 * The two first variables represents the statuses of room; if it's created and if its connected to the "start room" or if its connected 
 * to a room which is connected to the "start room".
 * The other two variables represent the list of player and the list of items in the room.
 */

class Room() {
  var north: Boolean = false
  var east: Boolean = false
  var south: Boolean = false
  var west: Boolean = false

  var created = false
  var connected = false
  var playerList: List[String] = List()
  var itemList: List[Item] = List()
  //  var NPCList: List[NPC]

  /**
   * This method changes the rooms created status to true.
   *
   */
  def setCreated() = {
    created = true
  }

  /**
   * This method is used to check the creation status of room. The returned value is a boolean.
   *
   */
  def isCreated(): Boolean = {
    return created
  }

  /**
   * This method changes a rooms connected status to true.
   *
   */
  def setConnected() = {
    connected = true
  }

  /**
   * This method is used to check the connection status of a room. The returned value is a boolean
   *
   */
  def isConnected(): Boolean = {
    return connected
  }

  /**
   * This method adds a connection to another room that is north of the current one.
   *
   */
  def northConnected = {
    north = true
  }

  /**
   * This method adds a connection to another room that is south of the current one.
   *
   */
  def southConnected = {
    south = true
  }

  /**
   * This method adds a connection to another room that is west of the current one.
   *
   */
  def westConnected = {
    west = true
  }

  /**
   * This method adds a connection to another room that is east of the current one.
   *
   */
  def eastConnected = {
    east = true
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
    return playerList.contains(player)
  }

  /**
   * This method adds an item to the list of item in a room.
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

}
