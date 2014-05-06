package lostvaults.server
import akka.actor.{ Actor, ActorRef, Props }
import scala.collection.mutable.HashMap
import akka.actor.actorRef2Scala

sealed trait PMapMsg
case class PMapAddPlayer(name: String, ref: ActorRef) extends PMapMsg
case class PMapRemovePlayer(name: String) extends PMapMsg
case class PMapGetPlayer(name: String, purpose: String) extends PMapMsg
case class PMapGetPlayerResponse(player: Option[ActorRef], purpose: String) extends PMapMsg
case class PMapIsOnline(name: String, purpose: String) extends PMapMsg
case class PMapIsOnlineResponse(online: Boolean, purpose: String) extends PMapMsg
case class PMapSendGameMessage(name: String, msg: GameMsg) extends PMapMsg
case object PMapSuccess extends PMapMsg
case object PMapFailure extends PMapMsg
case object PMapStartUp extends PMapMsg
case object PMapStarted extends PMapMsg

class PlayerMap extends Actor {
  var PMap: HashMap[String, ActorRef] = HashMap()

  def receive = {
    case PMapSendGameMessage(name: String, msg: GameMsg) => {
      val sendTo = PMap.find((A: Tuple2[String, ActorRef]) => A._1 == name)
      if (sendTo.isEmpty) {
        sender ! PMapFailure
      }
      else {
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
