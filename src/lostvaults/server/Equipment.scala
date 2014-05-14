package lostvaults.server
import EquipType._
/**
 * The class Equipment is a constructor for equipable items in the game. An
 * equipment is composed of value equiptype and variable effect, either
 * defence or attack.
 */

class Equipment(_name: String, _value: Int, _equipType: EquipType, _effect: Int) extends Item(_name: String, _value: Int) {
  val equipType = _equipType
  var effect = _effect
  
 /**
  * This method returns the equiptype of an equipment.
  *
  */
  def getEquipType = {
    equipType
  }
 
 /**
  * This method returns the effect of an equipment.
  *
  */
  def getEffect = {
    effect
  }

  /**
  * This method sets effect of an equipment
  * @param _effect The value of effect to be set on an equipment.
  *
  */
  def setEffect(_effect: Int) {
    effect = _effect
  }

}