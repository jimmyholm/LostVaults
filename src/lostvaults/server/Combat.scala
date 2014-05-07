package lostvaults.server
import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._

sealed trait CombatEvent
case class AddPlayer(name: String, speed: Int) extends CombatEvent
case class AttackPlayer(target: String) extends CombatEvent
case object DrinkPotion extends CombatEvent
sealed trait CombatState
case object Rest extends CombatState
case object Action extends CombatState
sealed trait CombatData
case class RestData(PlayerList: List[Tuple2[String, Int]], Duration: Int) extends CombatData
case class ActionData(PlayerList: List[Tuple2[String, Int]], TurnList: List[String], Duration: Int) extends CombatData

class Combat extends Actor with FSM[CombatState, CombatData] {
  val PMap = Main.PMap.get

  startWith(Rest, RestData(List(), 0))

  when(Rest, stateTimeout = 10.milliseconds) {
    case Event(AddPlayer(name, speed), data: RestData) => {
      val NextList = data.PlayerList :+ Tuple2[String, Int](name, speed)
      goto(Rest) using RestData(NextList.sortWith((a, b) => a._2 < b._2), data.Duration)
    }

    case Event(StateTimeout, data: RestData) => {
      val nextDuration = data.Duration + 1
      var turnList: List[String] = List()
      data.PlayerList.foreach(c => if (nextDuration % c._2 == 0) c._1 :: turnList)
      if (turnList isEmpty) {
        goto(Rest) using RestData(data.PlayerList, nextDuration)
      } else {
        goto(Action) using ActionData(data.PlayerList, turnList, data.Duration)
      }
    }
  }

  onTransition {
    case _ -> Action => {
      var player = nextStateData.asInstanceOf[ActionData].TurnList.head
      PMap ! PMapSendGameMessage(player, GameYourTurn)
    }
  }

  when(Action) {
    case Event(AttackPlayer(target), data: ActionData) => {
      val nextPlayer = data.TurnList.head
      val turnList = data.TurnList.tail
      if (turnList isEmpty)
        goto(Rest) using RestData(data.PlayerList, data.Duration)
      else
        goto(Action) using ActionData(data.PlayerList, turnList, data.Duration)
    }
    case Event(DrinkPotion, data: ActionData) => {
      val turnList = data.TurnList.tail
      if (turnList isEmpty)
        goto(Rest) using RestData(data.PlayerList, data.Duration)
      else
        goto(Action) using ActionData(data.PlayerList, turnList, data.Duration)

    }
  }
}










