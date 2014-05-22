package lostvaults.server
import akka.actor.{ Actor, ActorRef, Props }
import akka.util.ByteString
import akka.io.{ Tcp }
import scala.util.Random
object NPC {
  def props(name: String, hp: Int, rating: Int): Props = Props(new NPC(name, hp, rating))
}


class NPC(_name: String, _hp: Int, _rating: Int) extends Actor {
  var rand = new Random(System.currentTimeMillis())
  var name = _name
  val PMap = Main.PMap.get
  var dungeon = self
  var hp = _hp
  var weapon = ItemRepo.getOneRandom("Weapon", _rating)
  var armor = ItemRepo.getOneRandom("Armor", _rating - 1)
  var gold = ItemRepo.getOneRandom("Treasure", _rating)
  var backPack: List[Item] = List()
  var target = ""
  var battle: Option[ActorRef] = None
  var currentRoom = -1

  def getSpeed = {
    println("This NPC:s rating: " + _rating)
    println("Weapon: " + weapon.toString() + " Armor: " + armor.toString)
    weapon.speed + armor.speed
  }
  def getAttack = {
    weapon.attack + armor.attack
  }
  def getDefense = {
    weapon.defense + armor.defense
  }

  def receive = {
    case GamePlayerJoinBattle(_battle, enemy) => {
      battle = Some(_battle)
      _battle ! AddPlayer(self, name, getSpeed, enemy)
      target = enemy
    }
    case GameYourTurn => {
      if (battle != None) {
        battle.get ! AttackPlayer(name, target, getAttack)
      }
    }
    case GameDamage(from, strength) => {
      var damage = strength - getDefense
      if (damage < 0) { damage = 0 }
      hp = hp - damage
      if (hp <= 0) {
        dungeon ! GameRemovePlayer(name)
        dungeon ! GameNotifyRoom(currentRoom, "NPC " + name + " has received " + damage + " damage from " + from + ". " + name + " has died.")
        if (battle != None) {
          battle.get ! RemovePlayer(name)
          battle = None
        }
      } else {
        dungeon ! GameNotifyRoom(currentRoom, "NPC " + name + " has received " + damage + " damage from " + from + ".")
        if (battle != None) {
          battle.get ! DamageAck
        }
      }
    }
    case GameCombatWin => {
      battle = None
    }
  }
}