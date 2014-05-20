package lostvaults.server

import scala.io.Source

/**
 * The class item is a constructor for items in the game. An item is
 * composed of the name of the item, and the value of it.
 */

class Item(ID: Int, Name: String, Attack: Int, Defense: Int, Speed: Int, Price: Int, ItemType: String) {
  val id = ID
  val name = Name
  val attack = Attack
  val defense = Defense
  val speed = Speed
  val price = Price
  val itemType = ItemType
  
  def isWeapon(): Boolean = {
    itemType.compareToIgnoreCase("weapon") == 0
  }
  
  def isArmor(): Boolean = {
    itemType.compareToIgnoreCase("armor") == 0
  }
  
  def isTreasure(): Boolean = {
    itemType.compareToIgnoreCase("treasure") == 0
  }
  
  def isFood(): Boolean = {
    itemType.compareToIgnoreCase("food") == 0
  }
  
  def isPotion(): Boolean = {
    itemType.compareToIgnoreCase("potion") == 0
  }
  
}