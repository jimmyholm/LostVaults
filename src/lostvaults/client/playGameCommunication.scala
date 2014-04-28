package lostvaults.client

object playGameCommunication {
	var game : playGame = null
	var gui = new GUI()
 
  def setGame(_game : playGame) {
	  game = _game
	}
	
	
  def sendMessage(msg : String){
	  game.sendMessage(msg)
  }
  
  def updateDynamicInfo(msg: String){
	  gui.updateDynamicInfo(msg)  
  }
}