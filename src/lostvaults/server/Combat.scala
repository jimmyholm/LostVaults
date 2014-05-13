package lostvaults.server
import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._

sealed trait CombatEvent
case class AddPlayer(name: String, speed: Int) extends CombatEvent
case class AttackPlayer(name: String, target: String, strength: Int) extends CombatEvent
case class DrinkPotion(name: String) extends CombatEvent
case class RemovePlayer(name: String) extends CombatEvent
case object NoOp extends CombatEvent
sealed trait CombatState
case object Rest extends CombatState
case object Action extends CombatState
case object ActionTransit extends CombatState
sealed trait CombatData
case class RestData(PlayerList: List[Tuple2[String, Int]], Duration: Int) extends CombatData
case class ActionData(PlayerList: List[Tuple2[String, Int]], TurnList: List[String], Duration: Int) extends CombatData

class Combat extends Actor with FSM[CombatState, CombatData] {
  val PMap = Main.PMap.get
  var dungeon: Option[ActorRef] = None

  startWith(Rest, RestData(List(), 0))

  when(Rest, stateTimeout = 500.milliseconds) {
    case Event(StateTimeout, data: RestData) => {
      val nextDuration = data.Duration + 1
      var turnList: List[String] = List()
      data.PlayerList.foreach(c => if (nextDuration % c._2 == 0) turnList = c._1 :: turnList)
      if (turnList isEmpty) {
        println("COMBAT: New round, no player up for their turn.")
        stay using RestData(data.PlayerList, nextDuration)
      } else {
        println("COMBAT: New round, players " + turnList + " up for their turn.")
        var player = turnList.head
        println("COMBAT: It's " + player + "'s turn to take an action.")
        PMap ! PMapSendGameMessage(player, GameYourTurn)
        goto(Action) using ActionData(data.PlayerList, turnList, nextDuration)
      }
    }
  }

  when(Action) {
    case Event(AttackPlayer(name, target, strength), data: ActionData) => {
      val nextPlayer = data.TurnList.head
      val turnList = data.TurnList.tail
      if (name == nextPlayer) {
        println("COMBAT: Attack message received from the player whose turn it is.")
        PMap ! PMapSendGameMessage(target, GameDamage(name, strength))
        if (turnList isEmpty) {
          println("COMBAT: TurnList is empty, returning to rest state.")
          goto(Rest) using RestData(data.PlayerList, data.Duration)
        } else {
          println("COMBAT: TurnList not empty, giving next player their turn.")
          var player = turnList.head
          println("COMBAT: It's " + player + "'s turn to take an action.")
          PMap ! PMapSendGameMessage(player, GameYourTurn)
          goto(Action) using ActionData(data.PlayerList, turnList, data.Duration)
        }
      } else {
        println("COMBAT: Attack message from a player whose turn it is not.")
        stay
      }
    }
    case Event(DrinkPotion(name), data: ActionData) => {
      val nextPlayer = data.TurnList.head
      val turnList = data.TurnList.tail
      if (name == nextPlayer) {
        println("COMBAT: DrinkPotion message received from the player whose turn it is.")
        PMap ! PMapSendGameMessage(nextPlayer, GameDrinkPotion)
        if (turnList isEmpty) {
          println("COMBAT: TurnList is empty, returning to rest state.")
          goto(Rest) using RestData(data.PlayerList, data.Duration)
        } else {
          println("COMBAT: TurnList not empty, giving next player their turn.")
          var player = turnList.head
          println("COMBAT: It's " + player + "'s turn to take an action.")
          PMap ! PMapSendGameMessage(player, GameYourTurn)
          goto(Action) using ActionData(data.PlayerList, turnList, data.Duration)
        }
      } else {
        stay
      }
    }
  }
  whenUnhandled {
    case Event(AddPlayer(name, speed), data: RestData) => {
      println("COMBAT: Adding player " + name + " with speed " + speed + ".")
      if (data.PlayerList.exists(x => x == Tuple2(name, speed))) {
        stay
      } else {
        val NextList = data.PlayerList :+ Tuple2[String, Int](name, speed)
        stay using RestData(NextList.sortWith((a, b) => a._2 < b._2), data.Duration)
      }
    }
    case Event(AddPlayer(name, speed), data: ActionData) => {
      println("COMBAT: Adding player " + name + " with speed " + speed + ".")
      if (data.PlayerList.exists(x => x == Tuple2(name, speed))) {
        stay
      } else {
        val NextList = data.PlayerList :+ Tuple2[String, Int](name, speed)
        stay using ActionData(NextList.sortWith((a, b) => a._2 < b._2), data.TurnList, data.Duration)
      }
    }
    case Event(RemovePlayer(name), data: RestData) => {
      println("COMBAT: Remove Player called for " + name + ".")
      val NextList = data.PlayerList.filterNot(x => x._1.equals(name))
      println("COMBAT: Players still in combat: " + NextList)
      if (NextList.tail isEmpty) {
        if (dungeon != None) {}
        dungeon.get ! GameNotifyDungeon(NextList.head._1 + " is the winner.")
        context stop self
      }
      goto(Rest) using RestData(NextList, data.Duration)
    }
    case Event(RemovePlayer(name), data: ActionData) => {
      println("COMBAT: Remove Player called for " + name + ".")
      val NextList = data.PlayerList.filterNot(x => x._1.equals(name))
      val NextTurnList = data.TurnList.filterNot(x => x.equals(name))
      println("COMBAT: Players still in combat: " + NextList)
      if (NextList.tail.isEmpty) {
        if (dungeon != None) {}
        dungeon.get ! GameNotifyDungeon(NextList.head._1 + " is the winner.")
        dungeon.get ! GameCombatFinished
        context stop self
      }
      var player = NextTurnList.head
      println("COMBAT: It's " + player + "'s turn to take an action.")
      PMap ! PMapSendGameMessage(player, GameYourTurn)
      goto(Action) using ActionData(NextList, NextTurnList, data.Duration)
    }
    case Event(_dungeon: ActorRef, _) => {
      dungeon = Some(_dungeon)
      stay
    }
    case Event(_, _) => {
      stay // Do nothing
    }
  }
}










