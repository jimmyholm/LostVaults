package lostvaults.server
/**
 * Player.scala
 * @Author Felix Färsjö, Jimmy Holm, Fredrik Larsson, Anna Nilsson, Philip Åkerfeldt
 * @Version 1.0
 */
import lostvaults.Parser
import akka.actor.{ Actor, ActorRef }
import akka.util.ByteString
import akka.io.{ Tcp }
import scala.util.Random
import scala.collection.mutable.Queue
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.JdbcBackend
import scala.slick.driver.SQLiteDriver.simple._
import Q.interpolation

/**
 * PlayerAction is the base trait for the possible internal combat states a player can be in.
 */
sealed trait PlayerAction
/**
 * PAttack represents a player's wish to continue attacking their set target.
 */
case object PAttack extends PlayerAction
/**
 * PDrinkPotion represents a player's wish to consume a potion to restore health.
 */
case object PDrinkPotion extends PlayerAction
/**
 * PDecide represents a player's wish to pause the combat situation and make a new decision.
 */
case object PDecide extends PlayerAction

/**
 * Player is the connection point between the server and the remote client connection. It is through player
 * all other systems communicate with clients. Player also keeps track of the various character attributes of players.
 */
class Player extends Actor {
  import JdbcBackend.Database
  import Tcp._
  import context.{ system, become, unbecome, dispatcher }
  val random = new Random
  var currentRoom = 0
  var connection = self
  val PMap = Main.PMap.get
  var name = ""
  var dungeon = self
  var maxhp = 20
  var hp = 20
  var potions = 10
  var food = 5
  var gold = 10
  var treasures: List[Item] = List()
  var weapon: Item = ItemRepo.getById(1)
  var armor: Item = ItemRepo.getById(2)
  var knownRooms: List[Int] = List()
  val helpList: List[String] = List("General: \n", "Say \n", "Whisper \n", "LogOut \n\n", "Combat help: \n", "Attack [PLAYER] \n", "drinkPotion\n", "Stop\n")
  var state: PlayerAction = PDecide
  var target = ""
  var battle: Option[ActorRef] = None
  var msgQueue: Queue[String] = Queue()
  var waitForAck: Boolean = false
  var db: Option[Database] = None

  case object Ack extends Event
  case object SendNext
  case class PlayerData(id: Int, name: String, pass: String, maxHp: Int, weapon: Int, armor: Int, gold: Int)
  implicit val getPlayerResult = GetResult(r => PlayerData(r.nextInt, r.nextString, r.nextString, r.nextInt, r.nextInt, r.nextInt, r.nextInt))
  def equipItem(ID: Int) {
    val item = ItemRepo.getById(ID)
    if (item.id <= 0) {
      pushToNetwork("SYSTEM: You cannot equip that item.")
    } else {
      if (item.isWeapon) {
        weapon = item
      } else if (item.isArmor) {
        armor = item
      } else {
        pushToNetwork("SYSTEM: You cannot equip that item.")
      }
    }
  }
  def sendStats {
    pushToNetwork("HEALTHSTATS HP: " + hp + "/" + maxhp + " Food: " + food + " Gold: " + gold)
    pushToNetwork("COMBATSTATS Attack: " + getAttack + " Defense: " + getDefense + " Speed: " + getSpeed)
  }
  def savePlayer {
    implicit val session = db.get.createSession()
    val sql = "UPDATE players " +
      "SET maxHP=" + maxhp + ", weapon=" + weapon.id + ", armor=" + armor.id + ", gold=" + gold + " " +
      "WHERE name='" + name + "'"

    Q.updateNA(sql).execute
    session.close()
  }

  def getAttack: Integer = {
    weapon.attack + armor.attack
  }

  def getDefense: Integer = {
    weapon.defense + armor.defense
  }

  def getSpeed: Integer = {
    weapon.speed + armor.speed
  }

  def costToMove(cell: Int): Int = {
    if (knownRooms.exists(a => a == cell))
      0
    else
      1
  }
  def clearKnownRooms {
    knownRooms = List()
  }
  def pushToNetwork(msg: String) {
    if (waitForAck) {
      msgQueue.enqueue(msg)
    } else {
      waitForAck = true
      connection ! Write(ByteString(msg))
    }
  }

  def receive = {
    case Received(msg) => {
      connection = sender
      db = Some(Database.forURL("jdbc:sqlite:lostvaults.db", driver = "org.sqlite.JDBC"))
      val decodedMsg = msg.decodeString(java.nio.charset.StandardCharsets.UTF_8.name)
      println("(Player ["+ name + "]) sent message: " + decodedMsg)
      if (Parser.findWord(decodedMsg, 0) == "LOGIN") {
        name = Parser.findWord(decodedMsg, 1)
        PMap ! PMapIsOnline(name, decodedMsg)
      }
    }
    case PMapIsOnlineResponse(response, purpose) =>
      {
        println("PMap Response.")
        if (Parser.findWord(purpose, 0) == "LOGIN") {
          println("Login detected.")
          if (response) {
            pushToNetwork("LOGINFAIL")
            context stop self
          } else {
            implicit val session = db.get.createSession()
            // This guy's not online, check if the passwords match.
            val pass = Parser.findRest(purpose, 1)
            var sql = ""
            sql = "SELECT * FROM Players WHERE name='" + Parser.findWord(purpose, 1) + "'"
            var res = Q.queryNA[PlayerData](sql)
            if (res.list().isEmpty) { // Player not registered, so add to the database.
              sql = "INSERT INTO Players (name, pass, maxHP, weapon, armor, gold) " +
                "VALUES ('" + name + "', '" + pass + "', " + maxhp + ",1, 2, 20)"
              (Q.u + sql).execute
              pushToNetwork("LOGINOK")
              PMap ! PMapAddPlayer(name, self)
              dungeon = Main.City.get
              dungeon ! GameAddPlayer(name)
              self ! GameMoveToDungeon(dungeon)
              sendStats
              become(LoggedIn)
              session.close()
            } else { // Player DOES exist, compare passwords.
              println("Player IS registered.")
              var player = res.list().head
              if (player.pass == pass) { // Passwords match!
                hp = player.maxHp;
                food = 5
                potions = 5
                weapon = ItemRepo.getById(player.weapon)
                armor = ItemRepo.getById(player.armor)
                name = player.name
                savePlayer
                pushToNetwork("LOGINOK")
                PMap ! PMapAddPlayer(name, self)
                dungeon = Main.City.get
                dungeon ! GameAddPlayer(name)
                sendStats
                become(LoggedIn)
                session.close()
              } else { // Passwords do NOT match.
                pushToNetwork("SYSTEM Invalid Password!")
                pushToNetwork("LOGINFAIL")
                session.close()
                context stop self
              }
            }
          }
        }
      }

      // Här finns Receive satsen för servern - här tar vi emot alla användar-meddelanden från GUI:t

      def LoggedIn: Receive = {
        case SendNext => {
          if (msgQueue isEmpty)
            waitForAck = false
          else {
            val msg = msgQueue.dequeue
            connection ! Write(ByteString.apply(msg, java.nio.charset.StandardCharsets.UTF_8.name()))
          }
        }
        case Received(msg) => {
          val decodedMsg = msg.decodeString(java.nio.charset.StandardCharsets.UTF_8.name())
          println("(Player[" + name + "]) sent message: " + decodedMsg)
          val action = Parser.findWord(decodedMsg, 0).toUpperCase
          action match {
            case "ACK" => {
              self ! SendNext
            }
            case "SAY" => {
              dungeon ! GameSay(name, Parser.findRest(decodedMsg, 0))
            }
            case "WHISPER" => {
              if (name.compareToIgnoreCase(Parser.findWord(decodedMsg, 1)) == 0) {
                pushToNetwork("SYSTEM Stop talking to yourself, it makes you look crazy...")
                dungeon ! GameNotifyRoom(currentRoom, name + " mumbles something under their breath.")
              } else
                PMap ! PMapGetPlayer(Parser.findWord(decodedMsg, 1), decodedMsg)
            }
            case "LOGOUT" => {
              pushToNetwork("Bye")
              connection ! Close
            }
            case "HELP" => {
              pushToNetwork(helpList.mkString)
            }
            case "EMOTE" => {
              dungeon ! GameNotifyRoom(currentRoom, name + " " + Parser.findRest(decodedMsg, 0))
            }
            case "ATTACK" => {
              if (Parser.findWord(decodedMsg, 1).equalsIgnoreCase(name)) {
                pushToNetwork("Don't hit yourself")
              } else {
                if (battle != None) {
                  battle.get ! AttackPlayer(name, Parser.findRest(decodedMsg, 0), getAttack)
                } else {
                  dungeon ! GameAttackPlayer(name, Parser.findRest(decodedMsg, 0))
                  //dungeon ! GameAttackPlayerInCombat(Parser.findWord(decodedMsg, 1))
                }
                target = Parser.findRest(decodedMsg, 0)
                state = PAttack
              }
            }
            case "DRINKPOTION" => {
              if (battle != None) {
                battle.get ! DrinkPotion(name)
                state = PDrinkPotion
              } else {
                self ! GameDrinkPotion
              }
            }
            case "STOP" => {
              state = PDecide
            }
            case "MOVE" => {
              val dirStr = Parser.findWord(decodedMsg, 1).toUpperCase
              val dir =
                dirStr match {
                  case "NORTH" => 0
                  case "EAST" => 1
                  case "SOUTH" => 2
                  case "WEST" => 3
                  case _ => -1
                }
              if (dir == -1)
                pushToNetwork("SYSTEM That is not a valid direction. Try North, East, West or South.")
              else {
                dungeon ! GamePlayerMove(name, dir, currentRoom)
              }
            }
            case "ENTER" => {
              dungeon ! GameEnterDungeon(name)
            }
            case "EXIT" => {
              dungeon ! GameExitDungeon(name)
            }
            case "JOIN" => {
              if (name.compareToIgnoreCase(Parser.findWord(decodedMsg, 1)) == 0)
                pushToNetwork("SYSTEM You cannot form a group with yourself.")
              else
                dungeon ! GMapJoin(name, Parser.findWord(decodedMsg, 1))
            }
            case "LEAVE" => {
              dungeon ! GMapLeave(name)
            }
            case "PICKUP" => {
              dungeon ! GamePickUpItem(Parser.findRest(decodedMsg, 0), weapon.id, armor.id, name, currentRoom)
            }
            case "DROP" => {
              var value = 0
              val rest = Parser.findRest(decodedMsg, 0)
              if (rest.compareToIgnoreCase("potion") == 0) {
                if (!Parser.findWord(decodedMsg, 2).equals(Parser.findWord(decodedMsg, 1))) {
                  try {
                    value = Parser.findWord(decodedMsg, 2).toInt
                  } catch {
                    case e: Exception => println("DropException caught: " + Parser.findWord(decodedMsg, 2))
                  }
                }
                if (potions < 1) {
                  pushToNetwork("SYSTEM You don't have any potions to drop!")
                } else {
                  potions = potions - 1
                  dungeon ! GameDropItem(new Item(-2, "Potion", 1, 0, 0, "Potion"), currentRoom)
                }
              } else if (rest.compareToIgnoreCase("food") == 0) {
                if (!Parser.findWord(decodedMsg, 2).equals(Parser.findWord(decodedMsg, 1))) {
                  try {
                    value = Parser.findWord(decodedMsg, 2).toInt
                  } catch {
                    case e: Exception => println("DropException caught: " + Parser.findWord(decodedMsg, 2))
                  }
                }
                if (food < value) {
                  pushToNetwork("SYSTEM You can't drop that much food!")
                } else {
                  food = food - value
                  dungeon ! GameDropItem(new Item(-2, "Food", value, 0, 0, "Food"), currentRoom)
                }
              } else {
                pushToNetwork("SYSTEM You can only drop potions and food!")

              }
            }
            case _ => {
              pushToNetwork("SYSTEM I have no idea what you're wanting to do.")
            }
          }
        }
        case GameAttackNotInRoom(_name) => {
          pushToNetwork("SYSTEM " + _name + " is not in room, so you cannot attack her/him")
        }
        case GamePlayerJoinBattle(_battle, enemy) => {
          println("PLAYER: GamePlayerJoinBattle received")
          battle = Some(_battle)
          _battle ! AddPlayer(self, name, getSpeed, enemy)
        }
        case GameYourTurn => {
          println("PLAYER: It is " + name + "'s turn")
          println("PLAYER: Current State is " + state)
          state match {
            case PAttack => {
              if (battle != None) {
                println("PLAYER: sending attack message to combat")
                battle.get ! AttackPlayer(name, target, getAttack)
              }
            }
            case PDrinkPotion => {
              if (battle != None) {
                battle.get ! DrinkPotion(name)
              }
            }
            case PDecide => {
              pushToNetwork("SYSTEM It's your turn")
            }
          }
        }
        case GameDrinkPotion => {
          if (potions < 1) {
            pushToNetwork("SYSTEM You don't have a potion!")
          } else {
            hp = hp + random.nextInt(6) + 5
            if (hp > maxhp) {
              hp = maxhp
              state = PDecide
              pushToNetwork("SYSTEM Your drank a potion, you now have HP: " + hp)
            }
          }
        }
        case GameUpdateItem(item) => {
          if (item.isWeapon) {
            weapon = item
          } else if (item.isArmor) {
            armor = item
          } else if (item.isFood) {
            food = food + item.attack
            sendStats
          } else if (item.isTreasure) {
            item :: treasures
          } else if (item.isPotion) {
            potions = potions + 1
          }
          pushToNetwork("SYSTEM You picked up " + item.name)

          sendStats
        }
        case GameDamage(from, strength) => {
          println("PLAYER: GameDamage received")
          var damage = strength - getDefense
          if (damage < 0) { damage = 0 }
          hp = hp - damage
          sendStats
          if (hp <= 0) {
            dungeon ! GameNotifyRoom(currentRoom, "Player " + name + " has received " + damage + " damage from " + from + ". " + name + " has died.")
            if (battle != None) {
              battle.get ! RemovePlayer(name)
              battle = None
            }
            treasures.foreach(n => (dungeon ! GameDropItem(n, currentRoom)))
            dungeon ! GameDropItem(new Item(-2, "Food", food, 0, 0, "Food"), currentRoom)
            treasures = List()
            food = 0
            target = ""
            pushToNetwork("SYSTEM \n " +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼\n" +
              "███▀▀▀██┼███▀▀▀███┼███▀█▄█▀███┼██▀▀▀\n" +
              "██┼┼┼┼┼██┼██┼┼┼┼┼┼┼██┼██┼┼┼█┼┼┼┼██┼██┼┼┼┼\n" +
              "██┼┼┼┼▄▄▄┼██▄▄▄▄▄██┼██┼┼┼▀┼┼┼┼██┼██▀▀▀\n" +
              "██┼┼┼┼┼██┼██┼┼┼┼┼┼┼██┼██┼┼┼┼┼┼┼┼██┼██┼┼┼┼\n" +
              "███▄▄▄██┼██┼┼┼┼┼┼┼██┼██┼┼┼┼┼┼┼┼██┼██▄▄▄\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼\n" +
              "███▀▀▀███┼▀███┼┼██▀┼██▀▀▀┼██▀▀▀▀██▄┼\n" +
              "██┼┼┼┼┼┼┼██┼┼┼██┼┼██┼┼██┼┼┼┼┼██┼┼┼┼┼┼┼██\n" +
              "██┼┼┼┼┼┼┼██┼┼┼██┼┼██┼┼██▀▀▀┼██▄▄▄▄▄▀▀┼\n" +
              "██┼┼┼┼┼┼┼██┼┼┼██┼┼█▀┼┼██┼┼┼┼┼██┼┼┼┼┼██┼┼\n" +
              "███▄▄▄███┼┼┼┼─▀█▀┼┼─┼██▄▄▄┼██┼┼┼┼┼┼██▄\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼██┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼██┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼████▄┼┼┼▄▄▄▄▄▄▄┼┼┼▄████┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼▀▀█▄█████████▄█▀▀┼┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼┼█████████████┼┼┼┼┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼┼██▀▀▀███▀▀▀██┼┼┼┼┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼┼██┼┼┼███┼┼┼██┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼┼█████▀▄▀█████┼┼┼┼┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼███████████┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼▄▄▄██┼┼█▀█▀█┼┼██▄▄▄┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼▀▀██┼┼┼┼┼┼┼┼┼┼┼┼┼██▀▀┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼▀▀┼┼┼┼┼┼┼┼┼┼┼┼┼┼▀▀┼┼┼┼┼┼┼┼┼┼┼\n" +
              "┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼┼")
              dungeon ! GameRemovePlayer(name)
          } else {
            dungeon ! GameNotifyRoom(currentRoom, "Player " + name + " has received " + damage + " damage from " + from + ".")
            if (battle != None) {
              battle.get ! DamageAck
            }
          }
        }
        case GameCombatWin => {
          pushToNetwork("SYSTEM You won that battle")
          battle = None
        }
        case GameMessage(msg) => {
          pushToNetwork(msg)
        }
        case GamePlayerLeft(playerName) => {
          pushToNetwork("DUNGEONLEFT " + playerName)
          if (playerName == target) {
            target = ""
            state = PDecide
          }
        }
        case GamePlayerEnter(name) => {
          pushToNetwork("DUNGEONJOIN " + name)
        }
        case GameSay(name, msg) => {
          pushToNetwork("SAY " + name + " " + msg)
        }
        case GameWhisper(from, to, msg) => {
          pushToNetwork("WHISPER " + from + " " + to + " " + msg)
        }
        case GameMoveToDungeon(dungeon) => {
          this.dungeon = dungeon
          this.dungeon ! GameAddPlayer(name)
          clearKnownRooms
        }
        case GameDungeonMove(room, start) => {
          if (!start)
            food -= costToMove(room)
          knownRooms = room :: knownRooms
          currentRoom = room
          println("Player " + name + " received dungeon move")
        }
        case GameSystem(msg) => {
          pushToNetwork("SYSTEM " + msg)
        }
        
        case GameHeal => {
          hp = maxhp
          sendStats
        }
        
        case GameHarm(amnt) => {
          hp -= amnt
          if (hp <= 0) hp = 1
          sendStats
        }
        
        case PMapGetPlayerResponse(player, purpose) => {
          val action = Parser.findWord(purpose, 0).toUpperCase
          action match {
            case "WHISPER" => {
              if (!player.isEmpty) {
                val to = Parser.findWord(purpose, 1)
                val msg = Parser.findRest(purpose, 1)
                player.get ! GameWhisper(name, to, msg)
                self ! GameWhisper(name, to, msg)
              } else
                pushToNetwork("SYSTEM No Such Player Online")
            }
          }
        }
        
        case GamePlayerSetTarget(newTarget) => {
          target = newTarget
        }

        case _: ConnectionClosed => {
          dungeon ! GameRemovePlayer(name)
          PMap ! PMapRemovePlayer(name)
          context stop self
        }
      }
  }
}
