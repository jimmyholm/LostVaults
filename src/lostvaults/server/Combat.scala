package lostvaults.server
import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._

sealed trait CombatEvent
case class AddPlayer(name: String, speed: Int, enemy: String) extends CombatEvent
case class AttackPlayer(name: String, target: String, strength: Int) extends CombatEvent
case class DrinkPotion(name: String) extends CombatEvent
case class RemovePlayer(name: String) extends CombatEvent
case object DamageAck extends CombatEvent
sealed trait CombatState
case object Rest extends CombatState
case object Action extends CombatState
case object WaitForDamageAck extends CombatState
sealed trait CombatData
case class RestData(PlayerList: List[Tuple2[String, Int]], Duration: Int, CombatsPerPlayer: List[Tuple2[String, List[String]]]) extends CombatData
case class ActionData(PlayerList: List[Tuple2[String, Int]], TurnList: List[String], Duration: Int, CombatsPerPlayer: List[Tuple2[String, List[String]]]) extends CombatData

class Combat extends Actor with FSM[CombatState, CombatData] {
  val PMap = Main.PMap.get
  var dungeon: Option[ActorRef] = None

  startWith(Rest, RestData(List(), 0, List()))

  when(Rest, stateTimeout = 500.milliseconds) {
    case Event(StateTimeout, data: RestData) => {
      val nextDuration = data.Duration + 1
      var turnList: List[String] = List()
      data.PlayerList.foreach(c => if (nextDuration % c._2 == 0) turnList = c._1 :: turnList)
      if (turnList isEmpty) {
        println("COMBAT: New round, no player up for their turn.")
        stay using RestData(data.PlayerList, nextDuration, data.CombatsPerPlayer)
      } else {
        println("COMBAT: New round, players " + turnList + " up for their turn.")
        var player = turnList.head
        println("COMBAT: It's " + player + "'s turn to take an action.")
        PMap ! PMapSendGameMessage(player, GameYourTurn)
        goto(Action) using ActionData(data.PlayerList, turnList, nextDuration, data.CombatsPerPlayer)
      }
    }
  }

  when(WaitForDamageAck) {
    case Event(DamageAck, data: ActionData) => {
      println("Action DamageAck Received")
      var player = data.TurnList.head
      println("COMBAT: It's " + player + "'s turn to take an action.")
      PMap ! PMapSendGameMessage(player, GameYourTurn)
      goto(Action) using data
    }
    case Event(DamageAck, data: RestData) => {
      println("Rest DamageAck Received")
      goto(Rest) using data
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
          goto(WaitForDamageAck) using RestData(data.PlayerList, data.Duration, data.CombatsPerPlayer)
        } else {
          println("COMBAT: TurnList not empty, giving next player their turn.")
          goto(WaitForDamageAck) using ActionData(data.PlayerList, turnList, data.Duration, data.CombatsPerPlayer)
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
          goto(Rest) using RestData(data.PlayerList, data.Duration, data.CombatsPerPlayer)
        } else {
          println("COMBAT: TurnList not empty, giving next player their turn.")
          var player = turnList.head
          println("COMBAT: It's " + player + "'s turn to take an action.")
          PMap ! PMapSendGameMessage(player, GameYourTurn)
          goto(Action) using ActionData(data.PlayerList, turnList, data.Duration, data.CombatsPerPlayer)
        }
      } else {
        stay
      }
    }
  }
  whenUnhandled {
    case Event(AddPlayer(name, speed, enemy), data: RestData) => {
      println("COMBAT: Rest Adding player " + name + " with speed " + speed + ".")
      var combatList = data.CombatsPerPlayer
      if (!(combatList exists (c => c._1 == name)))
        combatList = (name, List()) :: combatList
      //Returns a new list which has added enemy to name's CombatsPerPlayer, unless enemy is already in list
      combatList = combatList map (c => if (c._1 == name) { if (!(c._2 exists (d => d == enemy))) { (c._1, enemy :: c._2) } else { c } } else { c })
      //Returns a new list which has added name to enemy's CombatsPerPlayer, unless name is already in list
      combatList = combatList map (c => if (c._1 == enemy) { if (!(c._2 exists (d => d == name))) { (c._1, name :: c._2) } else { c } } else { c })
      println("COMBAT: Rest Addplayer combatList: " + combatList)
      if (data.PlayerList.exists(x => x == Tuple2(name, speed))) {
        stay using RestData(data.PlayerList, data.Duration, combatList)
      } else {
        val NextList = data.PlayerList :+ Tuple2[String, Int](name, speed)
        stay using RestData(NextList.sortWith((a, b) => a._2 < b._2), data.Duration, combatList)
      }
    }
    case Event(AddPlayer(name, speed, enemy), data: ActionData) => {
      println("COMBAT: Action Adding player " + name + " with speed " + speed + ".")
      var combatList = data.CombatsPerPlayer
      if (!(combatList exists (c => c._1 == name)))
        combatList = (name, List()) :: combatList
      //Returns a new list which has added enemy to name's CombatsPerPlayer, unless enemy is already in list
      combatList = combatList map (c => if (c._1 == name) { if (!(c._2 exists (d => d == enemy))) { (c._1, enemy :: c._2) } else { c } } else { c })
      //Returns a new list which has added name to enemy's CombatsPerPlayer, unless name is already in list
      combatList = combatList map (c => if (c._1 == enemy) { if (!(c._2 exists (d => d == name))) { (c._1, name :: c._2) } else { c } } else { c })
      println("COMBAT: Action Addplayer combatList: " + combatList)
      var player = data.TurnList.head
      println("COMBAT: It's " + player + "'s turn to take an action.")
      PMap ! PMapSendGameMessage(player, GameYourTurn)
      if (data.PlayerList.exists(x => x == Tuple2(name, speed))) {
        goto(Action) using ActionData(data.PlayerList, data.TurnList, data.Duration, combatList)
      } else {
        val nextList = data.PlayerList :+ Tuple2[String, Int](name, speed)
        goto(Action) using ActionData(nextList sortWith ((a, b) => a._2 < b._2), data.TurnList, data.Duration, combatList)
      }
    }

    case Event(RemovePlayer(name), data: RestData) => {
      var combatList = data.CombatsPerPlayer
      //Removes name's combatlists
      combatList = combatList filterNot (c => c._1 == name)
      //Removes name from other players combatLists
      combatList = combatList map (c => (c._1, c._2 filterNot (d => d == name)))
      println("COMBAT: Remove Player called for " + name + ".")
      var NextPlayerList = data.PlayerList.filterNot(x => x._1.equals(name))
      println("COMBAT: Players still in combat: " + NextPlayerList)
      //Removes players that has emptied their combatPerPlayer list, and notifies them that they won
      combatList foreach (c => if (c._2 isEmpty) { PMap ! PMapSendGameMessage(c._1, GameCombatWin); NextPlayerList = NextPlayerList filterNot (d => d._1 == c._1) })
      combatList = combatList filterNot (c => c._2 isEmpty)
      println("COMBAT: RemovePlayer combatList RestData: " + combatList)
      if (NextPlayerList isEmpty) {
        if (dungeon != None) {
          dungeon.get ! GameNotifyDungeon(" Combat has ended ")
          dungeon.get ! GameCombatFinished
        }
        context stop self
      }
      goto(Rest) using RestData(NextPlayerList, data.Duration, combatList)
    }
    case Event(RemovePlayer(name), data: ActionData) => {
      var combatList = data.CombatsPerPlayer
      //Removes name's combatlists
      combatList = combatList filterNot (c => c._1 == name)
      //Removes name from other players combatLists
      combatList = combatList map (c => (c._1, c._2 filterNot (d => d == name)))
      println("COMBAT: Remove Player called for " + name + ".")
      var NextPlayerList = data.PlayerList.filterNot(x => x._1.equals(name))
      var NextTurnList = data.TurnList.filterNot(x => x.equals(name))

      //Removes players that has emptied their combatPerPlayer list, and notifies them that they won
      combatList foreach (c => if (c._2 isEmpty) { PMap ! PMapSendGameMessage(c._1, GameCombatWin); NextPlayerList = NextPlayerList filterNot (d => d._1 == c._1) })
      combatList foreach (c => if (c._2 isEmpty) { NextTurnList = NextTurnList filterNot (d => d == c._1) })
      println("COMBAT: Players still in combat: " + NextPlayerList)
      combatList = combatList filterNot (c => c._2 isEmpty)
      println("COMBAT: RemovePlayer combatList ActionData: " + combatList)

      if (NextPlayerList isEmpty) {
        if (dungeon != None) {
          dungeon.get ! GameNotifyDungeon(" Combat has ended ")
          dungeon.get ! GameCombatFinished
        }
        context stop self
      }
      var player = NextTurnList.head
      println("COMBAT: It's " + player + "'s turn to take an action.")
      PMap ! PMapSendGameMessage(player, GameYourTurn)
      goto(Action) using ActionData(NextPlayerList, NextTurnList, data.Duration, combatList)
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










