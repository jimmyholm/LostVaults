package lostvaults.server



class Room (_north : Boolean, _east: Boolean, _south: Boolean, _west: Boolean) {
  var north = _north
  var east = _east
  var south = _south
  var west = _west

  var created = false
  var connected = false
  
  def setCreated() = {
    created = true
  }
  
  def isCreated() : Boolean = {
    return created
  }
  
  def setConnected() = {
    connected = true
  }
  
  def isConnected() : Boolean = {
    return connected
  }
  
  def northConnected = {
    north = true
  }
  def southConnected = {
    south = true
  }  
  def westConnected = {
    west = true
  } 
  def eastConnected = {
    east = true
  }  
  
  
  var playerList: List[String] = List()
  var itemList: List[Item] = List()
//  var NPCList: List[NPC]
    
  
  def addPlayer(player: String) = {
    player::playerList
  }
  
  def removePlayer(player: String) = {
    playerList = playerList.filterNot((c => c == player))
  }
  
  def getPlayerList() : List[String] = {
    return playerList
  }
  
  def hasPlayer(player: String): Boolean = {
    return playerList.contains(player)
  }
  
  
  def addItem(newItem : Item) = {
    newItem::itemList
  }
  
  def removeItem(item: Item) = {
    itemList = itemList.filterNot((c => c == item))
  }
  
  def getItemList() : List[Item] = {
    return itemList
  }
  
  def hasItem(item: Item) : Boolean = {
    return itemList.contains(item)
  }
  
  
//  def addNPC(newNPC: NPC) = {
//    newNPC::NPCList
//  }
//  
//  def removeNPC(nPC: NPC) = {
//    NPCList = NPCList.filterNot((c => c == nPC))
//  }
//  
//  def getNPCList() : List[NPC] = {
//    return NPCList
//  }
//  
//  def hasNPC(nPC: NPC) : Boolean = {
//    return NPCList.contains(nPC)
//  }
  
}