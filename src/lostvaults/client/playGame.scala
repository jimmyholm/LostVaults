package lostvaults.client

import akka.actor.{ Actor, ActorRef, Props }
import lostvaults.Parser

object playGame {
  def props = Props(new playGame)
}

class playGame extends Actor {
  val TCPActorRef = context.actorOf(TCPClientxxx.props(self))
  val game = playGameCommunication
  game.setGame(this)

  def sendMessage(msg: String) {
    TCPActorRef ! msg
  }

  def receive = {
    case "Connect failed" => {
      game.updateDynamicInfo("\nConnect failed")
      context stop self
    }
    case "Connected" =>
      game.updateDynamicInfo("\nConnected")
    case c: String => {
      val firstWord = Parser.findWord(c, 0)
      firstWord match {
        case "LoginOK" =>
          game.updateDynamicInfo("\nYou are logged in")
        case "LoginFail" =>
          game.updateDynamicInfo("\nI'm sorry, but you cannot use that username")
        case "Say" =>
          game.updateDynamicInfo("\n" + Parser.findWord(c, 1) + " says: " + Parser.findRest(c, 1))
        case "Bye" =>
          game.updateDynamicInfo("\nBye bye, have a good day")
        case "Whisper" =>
          game.updateDynamicInfo("\n" + Parser.findWord(c, 1) + " whispers: " + Parser.findRest(c, 2))
        case "System" =>
          game.updateDynamicInfo("\nSystem says: " + Parser.findRest(c, 0))
        case _ =>
          game.updateDynamicInfo("\n" + c)
      }
    }
    case _ =>
      println("Coolt")
  }
}