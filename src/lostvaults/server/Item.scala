package lostvaults.server

import scala.io.Source

/**
 * The class item is a constructor for items in the game. An item is
 * composed of the name of the item, and the value of it.
 */

class Item(ID: Int, Name: String, Attack: Int, Defense: Int, Speed: Int, ItemType: String) {
  val id = ID
  val name = Name
  val attack = Attack
  val defense = Defense
  val speed = Speed
  val itemType = ItemType

  def compareTo(_Item: Item): String = {
    compareTo(_Item.id)
  }
  
  def compareTo(ID: Int): String = {
    val item = ItemRepo.getById(ID)
    var ret = "The " + name +  " is "
    if(item.itemType.compareToIgnoreCase(itemType) != 0) {
      ret + "not comparable with the " + item.name + "."
    } else {
      if(isWeapon()) {
        if(attack == item.attack) {
          ret + "no stronger " +
          (if(speed == item.speed) {
            "and no faster "
          } else if(speed > item.speed) {
            "and slower "
          } else {
            "but faster "
          }) + "than the " + item.name + "."
        } else if (attack < item.attack) {
          ret + "weaker " +
          (if(speed == item.speed) {
            "and no faster "
          } else if(speed > item.speed) {
            "and slower "
          } else {
            "but faster "
          }) + "than the " + item.name + "."
        } else {
          ret + "stronger " +
            (if(speed == item.speed) {
              "but no faster " 
            }else if(speed > item.speed){
              "but slower "
            }else {
              "and faster "
            }) + "than the " + item.name + "."
        }
      } else if(isArmor){
        if(defense == item.defense) {
          ret + "no stronger " +
          (if(speed == item.speed) {
            "and offers no better movement "
          } else if(speed > item.speed) {
            "and offer less movement"
          } else {
            "but offers better movement"
          }) + "than the " + item.name + "."
        } else if (defense < item.defense) {
          ret + "weaker " +
          (if(speed == item.speed) {
            "and offers no better movement "
          } else if(speed > item.speed) {
            "and offers less movement "
          } else {
            "but offers better movement "
          }) + "than the " + item.name + "."
        } else {
          ret + "stronger " +
            (if(speed == item.speed) {
              "but offers no better movement " 
            }else if(speed > item.speed){
              "but offers less movement "
            }else {
              "and offers better movement "
            }) + "than the " + item.name + "."
        }
      } else {
        ret + "not comparable to the " + item.name
      }
    }
  }

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

  override def toString = {
    name
  }
}