package lostvaults.server
import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._

sealed trait CombatEvent
case class AddPlayer(name: String, speed: Int) extends CombatEvent
case class AttackPlayer(target: String, strength: Int) extends CombatEvent
case class DrinkPotion(name: String) extends CombatEvent
case class RemovePlayer(name: String) extends CombatEvent
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
    case Event(AttackPlayer(target, strength), data: ActionData) => {
      val nextPlayer = data.TurnList.head
      val turnList = data.TurnList.tail
      if (turnList isEmpty)
        goto(Rest) using RestData(data.PlayerList, data.Duration)
      else
        goto(Action) using ActionData(data.PlayerList, turnList, data.Duration)
    }
    case Event(DrinkPotion(name), data: ActionData) => {
      val nextPlayer = data.TurnList.head
      val turnList = data.TurnList.tail
      if (name == nextPlayer) {
        PMap ! PMapSendGameMessage(nextPlayer, GameDrinkPotion)
        if (turnList isEmpty)
          goto(Rest) using RestData(data.PlayerList, data.Duration)
        else
          goto(Action) using ActionData(data.PlayerList, turnList, data.Duration)
      } else {
        stay
      }
    }
  }
  whenUnhandled {
    case Event(AddPlayer(name, speed), data: RestData) => {
      if (data.PlayerList.exists(x => x == Tuple2(name, speed))) {
        stay
      } else {
        val NextList = data.PlayerList :+ Tuple2[String, Int](name, speed)
        stay using RestData(NextList.sortWith((a, b) => a._2 < b._2), data.Duration)
      }
    }
    case Event(AddPlayer(name, speed), data: ActionData) => {
      if (data.PlayerList.exists(x => x == Tuple2(name, speed))) {
        stay
      } else {
        val NextList = data.PlayerList :+ Tuple2[String, Int](name, speed)
        stay using ActionData(NextList.sortWith((a, b) => a._2 < b._2), data.TurnList, data.Duration)
      }
    }
    case Event(RemovePlayer(name), data: RestData) => {
      val NextList = data.PlayerList.filterNot(x => x._1.equals(name))
      stay using RestData(NextList, data.Duration)
    }
    case Event(RemovePlayer(name), data: ActionData) => {
      val NextList = data.PlayerList.filterNot(x => x._1.equals(name))
      val NextTurnList = data.TurnList.filterNot(x => x.equals(name))
      stay using ActionData(NextList, NextTurnList, data.Duration)
    }
  }
}










