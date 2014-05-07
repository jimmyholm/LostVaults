package lostvaults.server
import akka.actor.{ Actor, ActorRef, Props }
import scala.collection.mutable.HashMap

/**
 * PMapMsg acts as the base-trait for all PlayerMap related messages
 */
sealed trait PMapMsg


/**
 *
 *
 * @param
 * @param
 */
case class PMapAddPlayer(name: String, ref: ActorRef) extends PMapMsg
/**
 *
 * @param
 */
case class PMapRemovePlayer(name: String) extends PMapMsg
/**
 *
 * @param
 * @param
 */
case class PMapGetPlayer(name: String, purpose: String) extends PMapMsg
/**
 *
 * @param
 * @param
 */
case class PMapGetPlayerResponse(player: Option[ActorRef], purpose: String) extends PMapMsg
/**
 *
 * @param
 * @param
 */
case class PMapIsOnline(name: String, purpose: String) extends PMapMsg
/**
 *
 * @param
 * @param
 */
case class PMapIsOnlineResponse(online: Boolean, purpose: String) extends PMapMsg
/**
 *
 * @param
 * @param
 */
case class PMapSendGameMessage(name: String, msg: GameMsg) extends PMapMsg
/**
 * 
 */
case object PMapSuccess extends PMapMsg
/**
 * 
 */
case object PMapFailure extends PMapMsg
/**
 * 
 */
case object PMapStartUp extends PMapMsg
/**
 * 
 */
case object PMapStarted extends PMapMsg

/**
 * PMapAddPlayer adds a name->PlayerActor connection to the PlayerMap. On success, a PMapSuccess message is returned to the sender.
 * @param name The name of the player to be added, used as the key value.
 * @param ref The ActorRef of the PlayerActor, used as the data value.
 */
case class PMapAddPlayer(name: String, ref: ActorRef) extends PMapMsg
/**
 * PMapRemove player removes a player->PlayerActor connection from the PlayerMap. On success a PMapSuccess message is returned to the sender.
 * If no player with name is present in the map, PMapFailure is returned.
 * @param name The name of the player to remove. 
 */
case class PMapRemovePlayer(name: String) extends PMapMsg
/**
 * PMapGetPlayer is a request to retreive the actor reference of a player given their name. This message will return a PMapGetPlayerResponse 
 * mesage to the sender.
 * @param name The name of the player whose actor is requested.
 * @param purpose A check string which gets passed along to the sender with the response, to make it easier to handle multiple GetPlayer requests. 
 */
case class PMapGetPlayer(name: String, purpose: String) extends PMapMsg
/**
 * PMapGetPlayerResponse is sent as a response to a GetPlayer request.
 * @param player An option containing None if the requested player does not exist, or a reference to the requested Player Actor.
 * @param purpose A string denoting why the request was made, taken from the GetPlayer request and passed along back to sender. 
 */
case class PMapGetPlayerResponse(player: Option[ActorRef], purpose: String) extends PMapMsg
/**
 * PMapIsOnline is a request to see if a player with a particular name is currently logged into the server, i.e. if their name exists in the player
 * map. A PMapIsOnlineResponse message is returned to sender describing the result of the request.
 * @param name The name of the player whose online status is requested.
 * @param purpose A check string which gets passed along to the sender with the response, to make it easier to handle multiple GetPlayer requests.
 */
case class PMapIsOnline(name: String, purpose: String) extends PMapMsg
/**
 * PMapIsOnlineResponse is a response sent to a IsOnline request.
 * @param online A boolean which is true if the requested character is online and false if not.
 * @param purpose A string denoting why the request was made, taken from the GetPlayer request and passed along back to sender.
 */
case class PMapIsOnlineResponse(online: Boolean, purpose: String) extends PMapMsg
/**
 * PMapSendGameMessage is a request to relay a Game Message to a named character. PMapSuccess is returned if the player is online,
 * PMapFailed if not.
 * @param name The name of the character a message should be sent to.
 * @param msg The Game Message to send to the requested character.
 */
case class PMapSendGameMessage(name: String, msg: GameMsg) extends PMapMsg
/**
 * PMapSuccess is a message denoting a successful request.
 */
case object PMapSuccess extends PMapMsg
/**
 * PMapFailure is a message denoting an unsuccessful request.
 */
case object PMapFailure extends PMapMsg


/**
 *
 *
 */
class PlayerMap extends Actor {
  var PMap: HashMap[String, ActorRef] = HashMap()
  /**
   *
   */
  def receive = {
    case PMapSendGameMessage(name: String, msg: GameMsg) => {
      val sendTo = PMap.find((A: Tuple2[String, ActorRef]) => A._1 == name)
      if (sendTo.isEmpty) {
        sender ! PMapFailure
      } else {
        sendTo.get._2 ! msg
        sender ! PMapSuccess
      }
    }

    case PMapAddPlayer(name: String, ref: ActorRef) => {
      val exist = PMap.find((A: Tuple2[String, ActorRef]) => A._1 == name)
      if (exist.isEmpty) {
        PMap += Tuple2[String, ActorRef](name, ref)
        sender ! PMapSuccess
      } else {
        sender ! PMapFailure
      }
    }
    case PMapRemovePlayer(name: String) => {
      val exist = PMap.find((A: Tuple2[String, ActorRef]) => A._1 == name)
      if (exist.isEmpty) {
        sender ! PMapFailure
      } else {
        PMap -= name
        sender ! PMapSuccess
      }
    }
    case PMapGetPlayer(name: String, purpose: String) => {
      val exist = PMap.find((A: Tuple2[String, ActorRef]) => A._1 == name)
      if (exist.isEmpty)
        sender ! PMapGetPlayerResponse(None, purpose)
      else
        sender ! PMapGetPlayerResponse(Some((exist.get)._2), purpose)
    }
    case PMapIsOnline(name: String, purpose: String) => {
      val exist = PMap.find((A: Tuple2[String, ActorRef]) => A._1 == name)
      sender ! PMapIsOnlineResponse(!exist.isEmpty, purpose)
    }
  }
}
