package lostvaults.server
import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._
/**
 * Internal messages passed along to a combat
 */
sealed trait CombatEvent
/**
 * Passed along to a combat, notifying that a player should be added to that combat
 * @param name The name of the player that should be added to the combat
 * @param speed The speed of the player that should be added to the combat
 * @param enemy The player that name is attacking or the player name is being attacked by
 */
case class AddPlayer(name: String, speed: Int, enemy: String) extends CombatEvent
/**
 *
 */
case class AttackPlayer(name: String, target: String, strength: Int) extends CombatEvent
case class DrinkPotion(name: String) extends CombatEvent
case class RemovePlayer(name: String) extends CombatEvent
case object ActionAck extends CombatEvent
sealed trait CombatState
case object Rest extends CombatState
case object Action extends CombatState
case object WaitForAck extends CombatState
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
      var nextTurnList: List[String] = List()
      data.PlayerList.foreach(c => if (nextDuration % c._2 == 0) nextTurnList = c._1 :: nextTurnList)
      if (nextTurnList isEmpty) {
        println("COMBAT-Rest: New round, no player up for their turn.")
        stay using RestData(data.PlayerList, nextDuration, data.CombatsPerPlayer)
      } else {
        println("COMBAT-Rest: New round, players " + nextTurnList + " up for their turn.")
        val nextPlayer = nextTurnList.head
        println("COMBAT-Rest: It's " + nextPlayer + "'s turn to take an action.")
        PMap ! PMapSendGameMessage(nextPlayer, GameYourTurn)
        goto(Action) using ActionData(data.PlayerList, nextTurnList, nextDuration, data.CombatsPerPlayer)
      }
    }
    case Event(AddPlayer(name, speed, enemy), incomingData: RestData) => {
      println("AddPlayer received for: " + name)
      val data = addPlayer(name, speed, enemy, incomingData)
      goto(Rest) using RestData(data.PlayerList, data.Duration, data.CombatsPerPlayer)
    }
  }

  when(WaitForAck) {
    case Event(ActionAck, data: ActionData) => {
      println("COMBAT-WaitForAck-ActionAck: ActionAck Received")
      val nextTurnList = data.TurnList.tail
      if (nextTurnList isEmpty) {
        println("COMBAT-WaitForAck: TurnList is empty, returning to rest state.")
        goto(Rest) using RestData(data.PlayerList, data.Duration, data.CombatsPerPlayer)
      } else {
        println("COMBAT-WaitForAck: TurnList: " + data.TurnList + " nextTurnList: " + nextTurnList)
        val nextPlayer = nextTurnList.head
        println("COMBAT-WaitForAck: It's " + nextPlayer + "'s turn to take an action.")
        PMap ! PMapSendGameMessage(nextPlayer, GameYourTurn)
        goto(Action) using ActionData(data.PlayerList, nextTurnList, data.Duration, data.CombatsPerPlayer)
      }
    }
    case Event(RemovePlayer(name), data: ActionData) => {
      println("COMBAT-WaitForAck: Remove Player called for " + name + ".")
      var nextTurnList = data.TurnList.tail
      var combatList = data.CombatsPerPlayer
      //Removes name's combatlists
      combatList = combatList filterNot (c => c._1.equals(name))
      //Removes name from other players combatLists
      combatList = combatList map (c => (c._1, c._2 filterNot (d => d.equals(name))))
      var nextPlayerList = data.PlayerList.filterNot(x => x._1.equals(name))
      nextTurnList = data.TurnList.filterNot(x => x.equals(name))
      /**Now all ocurrences of name has been removed, lets remove all players that had name as their last enemy: **/

      //Removes players that has emptied their combatPerPlayer list from nextPlayerList and nextTurnList, and notifies them that they won
      combatList foreach (c => if (c._2 isEmpty) { PMap ! PMapSendGameMessage(c._1, GameCombatWin); nextPlayerList = nextPlayerList filterNot (d => d._1 == c._1) })
      combatList foreach (c => if (c._2 isEmpty) { nextTurnList = nextTurnList filterNot (d => d == c._1) })
      println("COMBAT-WaitForAck-RemovePlayer: Players still in combat: " + nextPlayerList)
      combatList = combatList filterNot (c => c._2 isEmpty)
      println("COMBAT-WaitForAck-RemovePlayer: RemovePlayer combatList ActionData: " + combatList)

      if (nextPlayerList isEmpty) {
        if (dungeon != None) {
          dungeon.get ! GameNotifyDungeon(" Combat has ended ")
          dungeon.get ! GameCombatFinished
        }
        context stop self
      }
      if (nextTurnList isEmpty) {
        println("COMBAT-WaitForAck-RemovePlayer: TurnList is empty, returning to rest state.")
        goto(Rest) using RestData(nextPlayerList, data.Duration, combatList)
      } else {
        println("COMBAT-WaitForAck-RemovePlayer: TurnList: " + data.TurnList + " nextTurnList: " + nextTurnList)
        var nextPlayer = nextTurnList.head
        println("COMBAT-WaitForAck: It's " + nextPlayer + "'s turn to take an action.")
        PMap ! PMapSendGameMessage(nextPlayer, GameYourTurn)
        goto(Action) using ActionData(nextPlayerList, nextTurnList, data.Duration, combatList)
      }
    }
    case Event(AddPlayer(name, speed, enemy), data: ActionData) => {
      println("AddPlayer received for: " + name)
      goto(WaitForAck) using addPlayer(name, speed, enemy, data)
    }
  }

  when(Action) {
    case Event(AttackPlayer(name, target, strength), data: ActionData) => {
      val turnPlayer = data.TurnList.head
      if (data.PlayerList.exists(c => c._1.equals(target))) {
        if (name == turnPlayer) {
          println("COMBAT-Action-AttackPlayer: Attack message received from the player whose turn it is.")
          PMap ! PMapSendGameMessage(target, GameDamage(name, strength))
          goto(WaitForAck)

        } else {
          println("COMBAT-Action-AttackPlayer: Attack message from a player whose turn it is not.")
          stay
        }
      } else {
        if (dungeon != None) {
          dungeon.get ! GameAttackPlayer(name, target)
        }
        stay
      }
    }
    case Event(DrinkPotion(name), data: ActionData) => {
      val turnPlayer = data.TurnList.head
      val nextTurnList = data.TurnList.tail
      if (name == turnPlayer) {
        println("COMBAT-Action-DrinkPotion: DrinkPotion message received from the player whose turn it is.")
        PMap ! PMapSendGameMessage(turnPlayer, GameDrinkPotion)
        if (nextTurnList isEmpty) {
          println("COMBAT-Action-DrinkPotion: TurnList is empty, returning to rest state.")
          goto(Rest) using RestData(data.PlayerList, data.Duration, data.CombatsPerPlayer)
        } else {
          val nextPlayer = nextTurnList.head
          println("COMBAT-Action-DrinkPotion: TurnList: " + data.TurnList + " nextTurnList: " + nextTurnList)
          println("COMBAT-Action-DrinkPotion: It's " + nextPlayer + "'s turn to take an action.")
          PMap ! PMapSendGameMessage(nextPlayer, GameYourTurn)
          goto(Action) using ActionData(data.PlayerList, nextTurnList, data.Duration, data.CombatsPerPlayer)
        }
      } else {
        println("COMBAT-Action-DrinkPotion: DrinkPotion message from a player whose turn it is not.")
        stay
      }
    }
    case Event(AddPlayer(name, speed, enemy), data: ActionData) => {
      println("AddPlayer received for: " + name)
      goto(Action) using addPlayer(name, speed, enemy, data)
    }
  }

  def addPlayer(name: String, speed: Int, enemy: String, incomingData: CombatData) = {
    println("COMBAT-addPlayerFunction: Adding player " + name + " with speed " + speed + ".")
    var data = ActionData(List(), List(), 0, List())
    if (incomingData.isInstanceOf[RestData]) {
      val restData = incomingData.asInstanceOf[RestData]
      data = ActionData(restData.PlayerList, List(), restData.Duration, restData.CombatsPerPlayer)
    }
    var combatList = data.CombatsPerPlayer
    if (!(combatList exists (c => c._1 == name))) {
      combatList = (name, List()) :: combatList
    }
    //Returns a new list which has added enemy to name's CombatsPerPlayer, unless enemy is already in list
    combatList = combatList map (c => if (c._1 == name) { if (!(c._2 exists (d => d == enemy))) { (c._1, enemy :: c._2) } else { c } } else { c })
    //Returns a new list which has added name to enemy's CombatsPerPlayer, unless name is already in list
    combatList = combatList map (c => if (c._1 == enemy) { if (!(c._2 exists (d => d == name))) { (c._1, name :: c._2) } else { c } } else { c })
    println("COMAT-addPlayerFunction: Addplayer combatList: " + combatList)
    if (data.PlayerList.exists(x => x == Tuple2(name, speed))) {
      println("COMBAT-addPlayerFunction: no new player. PlayerList: " + data.PlayerList + " TurnList: " + data.TurnList + " combatList: " + combatList)
      ActionData(data.PlayerList, data.TurnList, data.Duration, combatList)
    } else {
      val nextPlayerList = ((name, speed) :: data.PlayerList).sortWith((a, b) => a._2 < b._2)
      println("COMBAT-addPlayerFunction: New player was added. PlayerList: " + nextPlayerList + " TurnList: " + data.TurnList + " combatList: " + combatList)
      ActionData(nextPlayerList, data.TurnList, data.Duration, combatList)
    }
  }
}










