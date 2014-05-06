package lostvaults.server
import EquipType._

class Equipment(_name: String, _value: Int, _equipType: EquipType, _effect: Int) extends Item(_name: String, _value: Int) {
  val equipType = _equipType
  var effect = _effect

  //getters and setters
  def getEquipType = {
    equipType
  }
  def getEffect = {
    effect
  }
  def setEffect(_effect: Int) {
    effect = _effect
  }

}