package lostvaults.client

/**
 *
 *
 */
object playGameCommunication {
  var game: playGame = null
  var gui = new GUI()

  /**
   * This method 
   * @param _game
   */
  // MÅSTE UTÖKA DOKUMENTATIONEN FÖR DENNA METOD
  def setGame(_game: playGame) {
    game = _game
  }

  /**
   * This method will send the given message to the current active game.
   * @param msg The message that will be sent.
   */
  def sendMessage(msg: String) {
    game.sendMessage(msg)
  }

  /**
   * This method will send a given IP to the current active game.
   * @param msg The IP that will be sent to the game.
   */
  def sendIP(msg: String) {
    game.sendIP(msg)
  }

  /**
   * This method updates the dynamic field in the GUI with the message given.
   * @param msg The message that will be used to update the dynamic field.
   */
  def updateDynamicInfo(msg: String) {
    gui.updateDynamicInfo(msg)
  }

  def setHealthStats(stats: String) {
		gui.setHealthStats(stats)
	}
	def setCombatStats(stats: String) {
		gui.setCombatStats(stats)
	}
  /**
   * s
   * This method will replace the current list of players in the dungeon, shown by the GUI, with the given list of players.
   * @param playerList The players that are to be shown as present in the dungeon.
   */
  def setDungeonPlayers(playerList: String) {
    gui.setDungeonPlayers(playerList)
  }

  /**
   * This method will add a given player to the list of current players in the dungeon, shown by the GUI.
   * @param player The player to be added to the dungeon
   */
  def addDungeonPlayer(player: String) {
    updateDynamicInfo("Player " + player + " has joined the dungeon.")
    gui.addDungeonPlayer(player)
  }

  /**
   * This method will remove a given player from the list of current players in the dungeon, shown by the GUI.
   * @param player The player to be removed from the dungeon.
   */
  def removeDungeonPlayer(player: String) {
    updateDynamicInfo("Player " + player + " has left the dungeon.")
    gui.removeDungeonPlayer(player)
  }

  /**
   * This method will replace the current list of players in the room, shown by the GUI, with the given list of players.
   * @param playerList The players that are to be shown as present in the room.
   */
  def setRoomPlayers(playerList: String) {
    gui.setRoomPlayers(playerList)
  }

  /**
   * This method will add a given player to the list of current players in the room.
   * @param player The name of the player to be added to the room.
   */
  def addRoomPlayer(player: String) {
    gui.addRoomPlayer(player)
  }

  /**
   * This method will remove a given player from the list of current players in the room, shown by the GUI.
   * @param player The name of the player to be removed from the room.
   */
  def removeRoomPlayer(player: String) {
    gui.removeRoomPlayer(player)
  }

  /**
   * This method will replace the current list of non-playable characters in the room, shown by the GUI, with the given list of NPCs.
   * @param otherList The list of NPCs that are to be shown as present in the room.
   */
  def setNPCs(npcList: String) {
    gui.setNPCs(npcList)
  }

  /**
   * This method will add a given NPC to the list of current NPCs in the room.
   * @param npc The name of the NPC to be added to the room.
   */
  def addNPC(npc: String) {
    gui.addNPC(npc)
  }

  /**
   * This method will remove a given NPC from the list of current NPCs in the room.
   * @param npc The name of the NPC to be removed from the room.
   */
  def removeNPC(npc: String) {
    gui.removeNPC(npc)
  }

  /**
   * This method will replace the current list of items in the room, shown by the GUI, with the given list of items.
   * @param itemList The list of items that are to be shown as present in the room.
   */
  def setItems(itemList: String) {
    gui.setItems(itemList)
  }

  /**
   * This method will add a given item to the list of current items in the room.
   * @param item The name of the item to be added in the room.
   */
  def addItem(item: String) {
    gui.addItem(item)
  }

  /**
   * This method will remove a given item from the list of current items in the room.
   * @param item The name of the item to be removed from the room.
   */
  def removeItem(item: String) {
    gui.removeItem(item)
  }

  /**
   * This method will replace the current list of items in the room, shown by the GUI, with the given list of exits.
   * @param exits The list of exits that are to be shown as available in the room.
   */
  def setExits(exits: String) {
    gui.setExits(exits);
  }

  /**
   * This method returns the name of the player.
   */
  def getName(): String = {
    gui.getName()
  }
  
  /**
   * This method returns the entered password.
   */
  def getPass(): String = {
    gui.getPass()
  }
}
