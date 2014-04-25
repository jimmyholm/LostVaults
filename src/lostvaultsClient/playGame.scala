package lostvaultsClient
import lostvaults.Parser

import akka.actor.{Actor, ActorRef, Props}

object playGame {
	def props = Props(new playGame)
}

class playGame extends Actor {  
	val TCPActorRef = context.actorOf(TCPClientxxx.props)
	val game = new playGameCommunication(this)
 
 	def sendMessage (msg: String) {
	  TCPActorRef ! msg
	}
	
  def receive = {
    case c : String =>
      val firstWord = Parser.FindWord(c, 0)
      firstWord match  {
        case "LoginOK" =>
          // Do Something
        case "LoginFail" =>
          // Do Something
        case "Say" =>
        	// Do Something
        case "Bye" =>
        	// Do Something	
        case "Whisper" =>
          // Do Something
        case "System" =>
          // Do Something
      }
    case _ => 
      println("Coolt")	
  }
}