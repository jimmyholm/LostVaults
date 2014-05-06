package lostvaults.server

class Consumable(_name: String, _value: Int, _effect: Int) extends Item(_name: String, _value: Int) {
  var effect = _effect // this value is negative if the potion takes life and positive if it gives life

  
  
  // getters and setters
  def getEffect = {
    effect
  }
  def setEffect(_effect: Int) {
    effect = _effect
  }

}