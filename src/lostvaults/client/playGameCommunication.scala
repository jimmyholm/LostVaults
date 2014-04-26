package lostvaults.client

class playGameCommunication(_game : playGame) {
	val game = _game
	val gui = new GUI(this)
 
  
  def sendMessage(msg : String){
	  game.sendMessage(msg)
  }
  
  def updateDynamicInfo(msg: String){
	  gui.updateDynamicInfo(msg)  
  }
}