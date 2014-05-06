package lostvaults.server
import akka.actor.Actor
import akka.util.ByteString
import akka.io.{ Tcp }
import lostvaults.Parser

class Player extends Actor {
  import Tcp._
  import context.{ system, become, unbecome }
  var connection = self
  val PMap = Main.PMap.get
  var name = ""
  var dungeon = self
  var hp = 0
  var defense = 0
  var attack = 0
  var knownRooms = List()
  val helpList: List[String] = List("Say \n", "Whisper \n", "LogOut \n")

  def receive = {
    case Received(msg) => {
      connection = sender
      val decodedMsg = msg.decodeString(java.nio.charset.Charset.defaultCharset().name())
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
            case "Help" => {
              connection ! Write(ByteString(helpList.mkString))

            }
            case _ => {
              connection ! Write(ByteString("SYSTEM I have no idea what you're wanting to do."))
            }
          }
        }
        case GamePlayerEnter(name) => {
          connection ! Write(ByteString("SYSTEM Player " + name + " has entered the dungeon"))
          connection ! Write(ByteString("DUNGEONJOIN " + name))
        }
        case GamePlayerLeft(name) => {
          connection ! Write(ByteString("SYSTEM Player " + name + " has left the dungeon"))
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
