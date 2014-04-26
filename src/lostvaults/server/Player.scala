package lostvaults.server
import akka.actor.Actor
import akka.util.ByteString
import akka.io.{Tcp}
import lostvaults.Parser

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
      if (Parser.findWord(decodedMsg, 0) == "Login") {
        name = Parser.findWord(decodedMsg, 1)
        PMap ! PMapIsOnline(name, decodedMsg)
      }
    }
    case PMapIsOnlineResponse(response, purpose) =>
      {
        if (Parser.findWord(purpose, 0) == "Login") {
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
          val action = Parser.findWord(decodedMsg, 0)
          action match {
            case "Say" => {
              dungeon ! GameSay(name, Parser.findRest(decodedMsg, 0))
            }
            case "Whisper" => {
              //dungeon ! GameWhisper(name, Parser.FindWord(decodedMsg, 1), Parser.FindRest(decodedMsg, 1))
              whisperTo = Parser.findWord(decodedMsg, 1)
              whisperMsg = Parser.findRest(decodedMsg, 1)
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
          val action = Parser.findWord(purpose, 0)
          action match {
            case "Whisper" => {
              if (!player.isEmpty) {
                val to = Parser.findWord(purpose, 1)
                val msg = Parser.findRest(purpose, 1)
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