package lostvaults.client

object playGameCommunication {
  var game: playGame = null
  var gui = new GUI()

  def setGame(_game: playGame) {
    game = _game
  }

  def sendMessage(msg: String) {
    game.sendMessage(msg)
  }

  def updateDynamicInfo(msg: String) {
    gui.updateDynamicInfo(msg)
  }



  def setDungeonPlayers(playerList: String) {
    gui.setDungeonPlayers(playerList)
  }
  def addDungeonPlayer(player: String) {
    gui.addDungeonPlayer(player)
  }
  def removeDungeonPlayer(player: String) {
    gui.removeDungeonPlayer(player)
  }

  def setRoomPlayers(playerList: String) {
    gui.setRoomPlayers(playerList)
  }
  def addRoomPlayer(player: String) {
    gui.addRoomPlayer(player)
  }
  def removeRoomPlayer(player: String) {
    gui.removeRoomPlayer(player)
  }

  def setOthers(otherList: String) {
    gui.setOthers(otherList)
  }
  def addOther(other: String) {
    gui.addOther(other)
  }
  def removeOther(other: String) {
    gui.removeOther(other)
  }

  def setItems(itemList: String) {
    gui.setItems(itemList)
  }
  def addItem(item: String) {
    gui.addItem(item)
  }
  def removeItem(item: String) {
    gui.removeItem(item)
  }

  def setExits(exits: String) {
    gui.setExits(exits);
  }

  def getName(): String = {
    gui.getName()
  }
}
