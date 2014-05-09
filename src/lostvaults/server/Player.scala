package lostvaults.server
import akka.actor.{ Actor, ActorRef }
import akka.util.ByteString
import akka.io.{ Tcp }
import lostvaults.Parser
import scala.util.Random
sealed trait PlayerAction
case object PAttack extends PlayerAction
case object PDrinkPotion extends PlayerAction
case object PDecide extends PlayerAction

class Player extends Actor {
  val random = new Random
  import Tcp._
  import context.{ system, become, unbecome }
  var connection = self
  val PMap = Main.PMap.get
  var name = ""
  var dungeon = self
  var hp = 10
  var defense = 0
  var attack = 1
  var food = 0
  var speed = 3//3 + random.nextInt(3)
  var knownRooms: List[Tuple2[Int, Int]] = List()
  val helpList: List[String] = List("Say \n", "Whisper \n", "LogOut \n")
  var state: PlayerAction = PDecide
  var previousState: PlayerAction = PDecide
  var target = ""
  var battle: Option[ActorRef] = None

  def receive = {
    case Received(msg) => {
      connection = sender
      val decodedMsg = msg.decodeString(java.nio.charset.Charset.defaultCharset().name())
      println("(Player) Received message: " + decodedMsg)
      if (Parser.findWord(decodedMsg, 0) == "LOGIN") {
        name = Parser.findWord(decodedMsg, 1)
        PMap ! PMapIsOnline(name, decodedMsg)
      }
    }
    case PMapIsOnlineResponse(response, purpose) =>
      {
        if (Parser.findWord(purpose, 0) == "LOGIN") {
          if (response) {
            connection ! Write(ByteString("LOGINFAIL"))
            context stop self
          } else {
            connection ! Write(ByteString("LOGINOK"))
            PMap ! PMapAddPlayer(name, self)
            dungeon = Main.City.get
            dungeon ! GameAddPlayer(name)
            become(LoggedIn)
          }
        }
      }

      // Här finns Receive satsen för servern - här tar vi emot alla användar-meddelanden från GUI:t

      def LoggedIn: Receive = {
        case Received(msg) => {
          val decodedMsg = msg.decodeString(java.nio.charset.Charset.defaultCharset().name())
          println("(Player[" + name + "]) Received message: " + decodedMsg)
          val action = Parser.findWord(decodedMsg, 0).toUpperCase
          action match {
            case "SAY" => {
              dungeon ! GameSay(name, Parser.findRest(decodedMsg, 0))
            }
            case "WHISPER" => {
              PMap ! PMapGetPlayer(Parser.findWord(decodedMsg, 1), decodedMsg)
            }
            case "LOGOUT" => {
              connection ! Write(ByteString("Bye"))
              connection ! Close
            }
            case "HELP" => {
              connection ! Write(ByteString(helpList.mkString))
            }
            case "ATTACK" => {
              if (battle == None)
                dungeon ! GameAttackPlayer(name, Parser.findWord(decodedMsg, 1))
              else
                battle.get ! AttackPlayer(name, Parser.findWord(decodedMsg, 1), attack)
              target = Parser.findWord(decodedMsg, 1)
              state = PAttack
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
            case _ => {
              connection ! Write(ByteString("SYSTEM I have no idea what you're wanting to do."))
            }
          }
        }
        case GamePlayerJoinBattle(_battle) =>
          battle = Some(_battle)
          _battle ! AddPlayer(name, speed)
        case GameYourTurn => {
          state match {
            case PAttack => {
              if (battle != None) {
                battle.get ! AttackPlayer(name, target, attack)
                previousState = state
              }
            }
            case PDrinkPotion => {
              if (battle != None) {
                battle.get ! DrinkPotion
              }
            }
            case PDecide => {
              connection ! Write(ByteString("SYSTEM It's your turn"))
              previousState = state
            }
          }
        }
        case GameDrinkPotion => {
          hp = hp + 10
          state = previousState
        }
        case GameDamage(from, strength) => {
          var damage = strength - defense
          if (damage < 0) { damage = 0 }
          hp = hp - damage
          if (hp <= 0) {
            dungeon ! GameNotifyDungeon("Player " + name + " has received " + damage + " damage from " + from + ". " + name + " has died.")
            //dungeon ! GameHasDied(name)
            if (battle != None) {
              battle.get ! GameRemovePlayer(name)
              battle = None
            }
          } else {
            dungeon ! GameNotifyDungeon("Player " + name + " has received " + damage + " damage from " + from + ".")
          }
        }
        case GamePlayerEnter(name) => {
          connection ! Write(ByteString("DUNGEONJOIN " + name))
        }
        case GamePlayerLeft(name) => {
          connection ! Write(ByteString("DUNGEONLEFT " + name))
        }
        case GameSay(name, msg) => {
          connection ! Write(ByteString("SAY " + name + " " + msg))
        }
        case GameWhisper(from, to, msg) => {
          connection ! Write(ByteString("WHISPER " + from + " " + to + " " + msg))
        }
        case GameMoveToDungeon(dungeon) => {
          this.dungeon = dungeon
        }
        case GameSystem(msg) => {
          connection ! Write(ByteString("SYSTEM " + msg))
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
                connection ! Write(ByteString("SYSTEM No Such Player Online"))
            }
          }
        }
        case _: ConnectionClosed => {
          dungeon ! GameRemovePlayer(name)
          PMap ! PMapRemovePlayer(name)
          context stop self
        }
      }
  }
}
