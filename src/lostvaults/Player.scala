package lostvaults
import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString

sealed trait GameMsg
case class GameSay(name: String, msg: String) extends GameMsg
case class GameWhisper(from: String, to: String, msg: String) extends GameMsg
case class GameYell(name: String, msg: String) extends GameMsg
case class GameRemovePlayer(name: String) extends GameMsg
case class GameAddPlayer(name: String) extends GameMsg
case class GamePlayerLeft(name: String) extends GameMsg
case class GamePlayerEnter(name: String) extends GameMsg
case class GameMoveToDungeon(dungeon: ActorRef) extends GameMsg
case class GameSystem(msg: String) extends GameMsg
class Player extends Actor {
  import Tcp._
  import context.{ system, become, unbecome }
  var connection = self
  val PMap = main.PMap.get
  var name = ""
  var dungeon = self
  var whisperTo = ""
  var whisperMsg = ""
  def receive() = {
    case Received(msg) => {
      connection = sender
      val decodedMsg = msg.decodeString(java.nio.charset.Charset.defaultCharset().name())
      if (Parser.FindWord(decodedMsg, 0) == "Login") {
        name = Parser.FindWord(decodedMsg, 1)
        PMap ! PMapIsOnline(name, decodedMsg)
      }
    }
    case PMapIsOnlineResponse(response, purpose) =>
      {
        if (Parser.FindWord(purpose, 0) == "Login") {
          if (response) {
            connection ! Write(ByteString("LoginFail"))
            context stop self
          } else {
            connection ! Write(ByteString("LoginOk"))
            PMap ! PMapAddPlayer(name, self)
            dungeon ! GameAddPlayer(name)
            become(LoggedIn)
          }
        }
      }

      def LoggedIn: Receive = {
        case Received(msg) => {
          val decodedMsg = msg.decodeString(java.nio.charset.Charset.defaultCharset().name())
          val action = Parser.FindWord(decodedMsg, 0)
          action match {
            case "Say" => {
              dungeon ! GameSay(name, Parser.FindRest(decodedMsg, 0))
            }
            case "Whisper" => {
              //dungeon ! GameWhisper(name, Parser.FindWord(decodedMsg, 1), Parser.FindRest(decodedMsg, 1))
              whisperTo = Parser.FindWord(decodedMsg, 1)
              whisperMsg = Parser.FindRest(decodedMsg, 1)
              PMap ! PMapGetPlayer(whisperTo, decodedMsg)
            }
            case "LogOut" => {
              connection ! Write(ByteString("Bye"))
              connection ! Close
            }
            case _ => {
              connection ! Write(ByteString("System I have no idea what you're wanting to do."))
            }
          }
        }
        case GamePlayerEnter(name) => {
          connection ! Write(ByteString("System Player " + name + " has entered the dungeon"))
        }
        case GamePlayerLeft(name) => {
          connection ! Write(ByteString("System Player " + name + " has left the dungeon"))
        }
        case GameSay(name, msg) => {
          connection ! Write(ByteString("Say " + name + " " + msg))
        }
        case GameWhisper(from, to, msg) => {
          connection ! Write(ByteString("Whisper " + from + " " + to + " " + msg))
        }
        case GameMoveToDungeon(dungeon) => {
          this.dungeon = dungeon
        }
        case GameSystem(msg) => {
          connection ! Write(ByteString("System " + msg))
        }
        case PMapGetPlayerResponse(player, purpose) => {
          val action = Parser.FindWord(purpose, 0)
          action match {
            case "Whisper" => {
              if (!player.isEmpty) {
                val to = Parser.FindWord(purpose, 1)
                val msg = Parser.FindRest(purpose, 1)
                player.get ! GameWhisper(name, to, msg)
                self ! GameWhisper(name, to, msg)
              } else
                connection ! Write(ByteString("System No Such Player Online"))
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