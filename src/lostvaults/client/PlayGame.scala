package lostvaults.client

import akka.actor.{ Actor, ActorRef, Props }
import lostvaults.Parser
import java.net.InetSocketAddress

object playGame {
  def props = Props(new playGame)
}

class playGame extends Actor {
  val TCPActorRef = context.actorOf(TCPClient.props(self))
  val game = playGameCommunication
  game.setGame(this)

  def sendMessage(msg: String) {
    TCPActorRef ! msg
  }
  def sendIP(ip: String) {
    println(ip)
    TCPActorRef ! ConnectTo(new InetSocketAddress(ip, 51234))
  }
  def receive = {
    case "Connect failed" => {
      game.updateDynamicInfo("Connect failed\n")
      context stop self
    }
    case "Connected" =>
      game.updateDynamicInfo("Connected\n")
    case c: String => {
      println(c)
      val firstWord = Parser.findWord(c, 0)
      firstWord match {
        case "DUNGEONLIST" =>
          game.setDungeonPlayers(Parser.findRest(c, 0))
        case "DUNGEONJOIN" =>
          game.addDungeonPlayer(Parser.findRest(c, 0))
        case "DUNGEONLEFT" =>
          game.removeDungeonPlayer(Parser.findRest(c, 0))
        case "ROOMLIST" =>
          game.setRoomPlayers(Parser.findRest(c, 0))
        case "ROOMJOIN" =>
          game.addRoomPlayer(Parser.findRest(c, 0))
        case "ROOMLEFT" =>
          game.removeRoomPlayer(Parser.findRest(c, 0))
        case "OTHERLIST" =>
          game.setOthers(Parser.findRest(c, 0))
        case "OTHERJOIN" =>
          game.addOther(Parser.findRest(c, 0))
        case "OTHERLEFT" =>
          game.removeOther(Parser.findRest(c, 0))
        case "ITEMLIST" =>
          game.setItems(Parser.findRest(c, 0))
        case "ITEMJOIN" =>
          game.addItem(Parser.findRest(c, 0))
        case "ITEMLEFT" =>
          game.removeItem(Parser.findRest(c, 0))
        case "ROOMEXITS" =>
          game.setExits(Parser.findRest(c, 0))
        case "LOGINOK" =>
          game.updateDynamicInfo("You are logged in\n")
        case "LOGINFAIL" =>
          game.updateDynamicInfo("\nI'm sorry, but you cannot use that username")
        case "SAY" => {
          if (game.getName.equals(Parser.findWord(c, 1)))
            game.updateDynamicInfo("You say: " + Parser.findRest(c, 1) + "\n")
          else
            game.updateDynamicInfo(Parser.findWord(c, 1) + " says: " + Parser.findRest(c, 1) + "\n")
        }
        case "BYE" =>
          game.updateDynamicInfo("Bye bye, have a good day\n")
        case "WHISPER" =>
          game.updateDynamicInfo(Parser.findWord(c, 1) + " whispers to " + Parser.findWord(c, 2) + ": " + Parser.findRest(c, 2) + "\n")
        case "SYSTEM" =>
          game.updateDynamicInfo("System says: " + Parser.findRest(c, 0) + "\n")
        case _ =>
          game.updateDynamicInfo(c + "\n")
      }
    }
    case _ =>
      println("A misstake has happened\n")
  }
}
