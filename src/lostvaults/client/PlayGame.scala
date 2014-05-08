package lostvaults.client

import akka.actor.{ Actor, ActorRef, Props }
import lostvaults.Parser
import java.net.InetSocketAddress

/**
 * The object playGame is responsible for creating an instance of playGame in a new actor.
 *
 */
object playGame {

  /**
   * Creates a new actor for the object playGame
   *
   */
  def props = Props(new playGame)
}

/**
 * The playGame class is responsible for setting up the game and the references to the current game.
 *
 */
class playGame extends Actor {
  val TCPActorRef = context.actorOf(TCPClient.props(self))
  val game = playGameCommunication
  game.setGame(this)

  /**
   * This method sends a message to an ActorRef in the TCP Client.
   * @param msg The message that will be sent.
   */
  def sendMessage(msg: String) {
    val action = Parser.findWord(msg, 0)
    val sendMsg = action.toUpperCase + " " + Parser.findRest(msg, 0)
    TCPActorRef ! sendMsg
  }

  /**
   * This method will print the IP given and it will also send a a connection request with the given IP to the TCP Clients ActorRef.
   * @param ip The IP that will be sent.
   */
  def sendIP(ip: String) {
	  TCPActorRef ! ConnectTo(new InetSocketAddress(ip, 51234))
  }

  /**
   * This is where messages from the TCP Client are received. 
   *
   * In this method we receive different messages from the TCP Client which all have a specific action connected to them. 
   * All of the actions earlier mentioned will have some specific effect on the current game.
   */
  def receive = {
    case "Connect failed" => {
      game.updateDynamicInfo("Connect failed")
      context stop self
    }
    case "Connected" =>
      game.updateDynamicInfo("Connected")
      val name = game.getName()
      //val passwd = game.getPassword()
      TCPActorRef ! "LOGIN " + name;
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
        case "NPCLIST" =>
          game.setNpcs(Parser.findRest(c, 0))
        case "NPCJOIN" =>
          game.addNpc(Parser.findRest(c, 0))
        case "NPCLEFT" =>
          game.removeNpc(Parser.findRest(c, 0))
        case "ITEMLIST" =>
          game.setItems(Parser.findRest(c, 0))
        case "ITEMJOIN" =>
          game.addItem(Parser.findRest(c, 0))
        case "ITEMLEFT" =>
          game.removeItem(Parser.findRest(c, 0))
        case "ROOMEXITS" =>
          game.setExits(Parser.findRest(c, 0))
        case "LOGINOK" =>
          game.updateDynamicInfo("You are logged in")
        case "LOGINFAIL" =>
          game.updateDynamicInfo("I'm sorry, but you cannot use that username")
        case "SAY" => {
          if (game.getName.equals(Parser.findWord(c, 1)))
            game.updateDynamicInfo("You say: " + Parser.findRest(c, 1))
          else
            game.updateDynamicInfo(Parser.findWord(c, 1) + " says: " + Parser.findRest(c, 1) )
        }
        case "BYE" =>
          game.updateDynamicInfo("Bye bye, have a good day")
        case "WHISPER" =>
          game.updateDynamicInfo(Parser.findWord(c, 1) + " whispers to " + Parser.findWord(c, 2) + ": " + Parser.findRest(c, 2))
        case "SYSTEM" =>
          game.updateDynamicInfo("System says: " + Parser.findRest(c, 0))
        case _ =>
          game.updateDynamicInfo(c)
      }
    }
    case _ =>
      println("A misstake has occurred")
  }
}
