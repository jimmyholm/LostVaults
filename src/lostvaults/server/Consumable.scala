package lostvaults.server

/**
 * The class Consumable is a constructor for items usable by player in the   * game. Variable effect is an integer that either adds or subtracts player  * health, depending on value of integer.
 */

class Consumable(_name: String, _value: Int, _effect: Int) extends Item(_name: String, _value: Int) {
  var effect = _effect // this value is negative if the potion takes life and positive if it gives life
}