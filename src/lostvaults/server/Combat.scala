/**
 * CheatActor.scala
 * @Author Felix Färsjö, Jimmy Holm, Fredrik Larsson, Anna Nilsson, Philip Åkerfeldt
 * @Version 1.0
 */
package lostvaults.server
import akka.actor.{ Actor, ActorRef, FSM, Props}
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
case class AddPlayer(self: ActorRef, name: String, speed: Int, enemy: String) extends CombatEvent
/**
 * Passed along to a combat, notifying that name wants to attach target with strength strength
 * @param name The name of the attacker
 * @param target The name of the attackee
 * @param strength the strength of the attack (corresponds to name's attack value)
 */
case class AttackPlayer(name: String, target: String, strength: Int) extends CombatEvent
/**
 * Passed along to a combat, notifying that name wants to drink a potion
 * @param name The name of the player who wants to drink a potion
 */
case class DrinkPotion(name: String) extends CombatEvent
/**
 * Passed along to a combat, notifying that name should be removed from the combat
 * @param name The name of the player who should be removed
 */
case class RemovePlayer(name: String) extends CombatEvent
/**
 * Passed along to a combat, notifying that a player has received it's damage
 */
case object DamageAck extends CombatEvent
/**
 * The states in combat
 */
sealed trait CombatState
/**
 * The Rest state: Can arrive to this state from Rest, Action and WaitForAck
 * This state keeps track of the turns, and create a list of all players that is up for their turn
 * From this state you can go Action or back to Rest
 * Before going from Rest to Action next player will be informed that it is her turn
 */
case object Rest extends CombatState
/**
 * The Action state: Can arrive to this state from Action, WaitForAck and Rest
 * This state waits for a message from the player who's turn it is
 * From this state you can go to Action, WaitForAck or Rest
 * Before going from this state to Action next player will be informed that it is her turn
 */
case object Action extends CombatState
/**
 * The WaitForAck state: Can arrive to this state from Action
 * This state wait for a message from the player who was attacked, acknowledging that the player
 * has received damage and calculated it's new HP. If the player is alive a DamageAck will be received.
 * If the player is dead a RemovePlayer will be received.
 * From this state you can go to Action or Rest
 * Before going from this state to Action next player will be informed that it is her turn
 */
case object WaitForAck extends CombatState
/**
 * The data that the states keep track of.
 */
sealed trait CombatData
/**
 * Data within Rest.
 * @param PlayerList A list with all players in combat, element in list looks like: Tuple2(name, speed)
 * @param Duration The game clock, increased by 1 with each turn
 * @param CombatsPerPlayer A list with all players in combat, and with each player a list of that player's enemies, element in list looks like: Tuple2(name, List(name of players))
 */
case class RestData(PlayerList: List[Tuple3[String, Int, ActorRef]], Duration: Int, CombatsPerPlayer: List[Tuple2[String, List[String]]]) extends CombatData
/**
 * Data within Action.
 * @param PlayerList All players in combat, element in list looks like: Tuple2(name, speed)
 * @param TurnList The players who are currently up for their turn
 * @param Duration The game clock, increased by 1 with each turn
 * @param CombatsPerPlayer A list with all players in combat, and with each player a list of that player's enemies, element in list looks like: Tuple2(name, List(name of players))
 */
case class ActionData(PlayerList: List[Tuple3[String, Int, ActorRef]], TurnList: List[String], Duration: Int, CombatsPerPlayer: List[Tuple2[String, List[String]]]) extends CombatData



object Combat {
  def props(dungeon: ActorRef, roomIndex: Int, dungeonid: Int, id: Int): Props = Props(new Combat(dungeon: ActorRef, roomIndex: Int, dungeonid:Int, id:Int))
}


class Combat (_dungeon: ActorRef, _roomIndex: Int, dungeonId: Int, id: Int) extends Actor with FSM[CombatState, CombatData] {
  var dungeon = _dungeon
  var roomIndex = _roomIndex
  var myID = id
  var dungeonID = dungeonId
  
  startWith(Rest, RestData(List(), 0, List()))

  when(Rest, stateTimeout = 500.milliseconds) {
    case Event(StateTimeout, data: RestData) => {
      val nextDuration = data.Duration + 1
      var nextTurnList: List[String] = List()
      data.PlayerList.foreach(c => if (nextDuration % c._2 == 0) nextTurnList = c._1 :: nextTurnList)
      if (nextTurnList isEmpty) {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Rest: New round, no player up for their turn.")
        stay using RestData(data.PlayerList, nextDuration, data.CombatsPerPlayer)
      } else {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Rest: New round, players " + nextTurnList + " up for their turn.")
        val nextPlayer = nextTurnList.head
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Rest: It's " + nextPlayer + "'s turn to take an action.")
        combatSendGameMsg(nextPlayer, data.PlayerList, GameYourTurn)
        goto(Action) using ActionData(data.PlayerList, nextTurnList, nextDuration, data.CombatsPerPlayer)
      }
    }
    case Event(AddPlayer(nameActorRef, name, speed, enemy), incomingData: RestData) => {
      println("(Combat " + dungeonID+"-"+myID + ") AddPlayer received for: " + name)
      val data = addPlayer(nameActorRef, name, speed, enemy, incomingData)
      goto(Rest) using RestData(data.PlayerList, data.Duration, data.CombatsPerPlayer)
    }
  }

  when(WaitForAck) {
    case Event(DamageAck, data: ActionData) => {
      println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck-ActionAck: ActionAck Received")
      val nextTurnList = data.TurnList.tail
      if (nextTurnList isEmpty) {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck: TurnList is empty, returning to rest state.")
        goto(Rest) using RestData(data.PlayerList, data.Duration, data.CombatsPerPlayer)
      } else {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck: TurnList: " + data.TurnList + " nextTurnList: " + nextTurnList)
        val nextPlayer = nextTurnList.head
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck: It's " + nextPlayer + "'s turn to take an action.")
        combatSendGameMsg(nextPlayer, data.PlayerList, GameYourTurn)
        goto(Action) using ActionData(data.PlayerList, nextTurnList, data.Duration, data.CombatsPerPlayer)
      }
    }
    case Event(RemovePlayer(name), data: ActionData) => {
      println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck: Remove Player called for " + name + ".")
      var nextTurnList = data.TurnList.tail
      var combatList = data.CombatsPerPlayer
      //Removes name's combatlists
      combatList = combatList filterNot (c => c._1.equals(name))
      //Removes name from other players combatLists
      combatList = combatList map (c => (c._1, c._2 filterNot (d => d.equals(name))))
      var nextPlayerList = data.PlayerList.filterNot(x => x._1.equals(name))
      nextTurnList = data.TurnList.filterNot(x => x.equals(name))
      dungeon ! GameRemoveFromRoom(name, roomIndex) 
      /**Now all ocurrences of name has been removed, lets remove all players that had name as their last enemy: **/
      
      //Removes players that has emptied their combatPerPlayer list from nextPlayerList and nextTurnList, and notifies them that they won
      combatList foreach (c => if (c._2 isEmpty) { combatSendGameMsg(c._1, data.PlayerList, GameCombatWin); nextPlayerList = nextPlayerList filterNot (d => d._1 == c._1) })
      combatList foreach (c => if (c._2 isEmpty) { nextTurnList = nextTurnList filterNot (d => d == c._1) })
      println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck-RemovePlayer: Players still in combat: " + nextPlayerList)
      combatList = combatList filterNot (c => c._2 isEmpty)
      println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck-RemovePlayer: RemovePlayer combatList ActionData: " + combatList)

      if (nextPlayerList isEmpty) {
          dungeon ! GameNotifyDungeon(" Combat has ended ")
          dungeon ! GameCombatFinished(roomIndex)
        context stop self
      }
      if (nextTurnList isEmpty) {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck-RemovePlayer: TurnList is empty, returning to rest state.")
        goto(Rest) using RestData(nextPlayerList, data.Duration, combatList)
      } else {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck-RemovePlayer: TurnList: " + data.TurnList + " nextTurnList: " + nextTurnList)
        var nextPlayer = nextTurnList.head
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-WaitForAck: It's " + nextPlayer + "'s turn to take an action.")
        combatSendGameMsg(nextPlayer, nextPlayerList, GameYourTurn)
        goto(Action) using ActionData(nextPlayerList, nextTurnList, data.Duration, combatList)
      }
    }
    case Event(AddPlayer(nameActorRef, name, speed, enemy), data: ActionData) => {
      println("(Combat " + dungeonID+"-"+myID + ") AddPlayer received for: " + name)
      goto(WaitForAck) using addPlayer(nameActorRef, name, speed, enemy, data)
    }
  }

  when(Action) {
    case Event(AttackPlayer(name, target, strength), data: ActionData) => {
      println("(Combat " + dungeonID+"-"+myID + ") COMBAT: AttackPlayer received, attacking " + target)
      val turnPlayer = data.TurnList.head
      if (data.PlayerList.exists(c => c._1.compareToIgnoreCase(target) == 0)) {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT: Target exists")
        if (name == turnPlayer) {
          println("(Combat " + dungeonID+"-"+myID + ") COMBAT: name == turnplayer")
          println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Action-AttackPlayer: Attack message received from the player whose turn it is.")
          combatSendGameMsg(target, data.PlayerList, GameDamage(name, strength))
          goto(WaitForAck)

        } else {
          println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Action-AttackPlayer: Attack message from a player whose turn it is not.")
          stay
        }
      } else {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Action-AttackPlayer: Tried to attack a player that is not in combat")
          dungeon ! GameAttackPlayer(name, target)
        stay
      }
    }
    case Event(DrinkPotion(name), data: ActionData) => {
      val turnPlayer = data.TurnList.head
      val nextTurnList = data.TurnList.tail
      if (name == turnPlayer) {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Action-DrinkPotion: DrinkPotion message received from the player whose turn it is.")
        combatSendGameMsg(turnPlayer, data.PlayerList, GameDrinkPotion)
        if (nextTurnList isEmpty) {
          println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Action-DrinkPotion: TurnList is empty, returning to rest state.")
          goto(Rest) using RestData(data.PlayerList, data.Duration, data.CombatsPerPlayer)
        } else {
          val nextPlayer = nextTurnList.head
          println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Action-DrinkPotion: TurnList: " + data.TurnList + " nextTurnList: " + nextTurnList)
          println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Action-DrinkPotion: It's " + nextPlayer + "'s turn to take an action.")
          combatSendGameMsg(nextPlayer, data.PlayerList, GameYourTurn)
          goto(Action) using ActionData(data.PlayerList, nextTurnList, data.Duration, data.CombatsPerPlayer)
        }
      } else {
        println("(Combat " + dungeonID+"-"+myID + ") COMBAT-Action-DrinkPotion: DrinkPotion message from a player whose turn it is not.")
        stay
      }
    }
    case Event(AddPlayer(nameActorRef, name, speed, enemy), data: ActionData) => {
      println("(Combat " + dungeonID+"-"+myID + ") AddPlayer received for: " + name)
      goto(Action) using addPlayer(nameActorRef, name, speed, enemy, data)
    }
  }

  def addPlayer(nameActorRef: ActorRef, name: String, speed: Int, enemy: String, incomingData: CombatData) = {
    println("(Combat " + dungeonID+"-"+myID + ") COMBAT-addPlayerFunction: Adding player " + name + " with speed " + speed + ".")
    var data = ActionData(List(), List(), 0, List())
    if (incomingData.isInstanceOf[RestData]) {
      val restData = incomingData.asInstanceOf[RestData]
      data = ActionData(restData.PlayerList, List(), restData.Duration, restData.CombatsPerPlayer)
    }
    var combatList = data.CombatsPerPlayer
    if (!(combatList exists (c => c._1.compareToIgnoreCase(name) == 0))) {
      combatList = (name, List()) :: combatList
    }
    //Returns a new list which has added enemy to name's CombatsPerPlayer, unless enemy is already in list
    combatList = combatList map (c => if (c._1.compareToIgnoreCase(name) == 0) { if (!(c._2 exists (d => d == enemy))) { (c._1, enemy :: c._2) } else { c } } else { c })
    //Returns a new list which has added name to enemy's CombatsPerPlayer, unless name is already in list
    combatList = combatList map (c => if (c._1.compareToIgnoreCase(enemy) == 0) { if (!(c._2 exists (d => d == name))) { (c._1, name :: c._2) } else { c } } else { c })
    println("(Combat " + dungeonID+"-"+myID + ") COMBAT-addPlayerFunction: Addplayer combatList: " + combatList)
    if (data.PlayerList.exists(x => x._1.compareToIgnoreCase(name) == 0)) {
      println("(Combat " + dungeonID+"-"+myID + ") COMBAT-addPlayerFunction: no new player. PlayerList: " + data.PlayerList + " TurnList: " + data.TurnList + " combatList: " + combatList)
      ActionData(data.PlayerList, data.TurnList, data.Duration, combatList)
    } else {
      val nextPlayerList = ((name, speed, nameActorRef) :: data.PlayerList).sortWith((a, b) => a._2 < b._2)
      println("(Combat " + dungeonID+"-"+myID + ") COMBAT-addPlayerFunction: New player was added. PlayerList: " + nextPlayerList + " TurnList: " + data.TurnList + " combatList: " + combatList)
      ActionData(nextPlayerList, data.TurnList, data.Duration, combatList)
    }
  }
  def combatSendGameMsg(name: String, playerList: List[(String, Int, ActorRef)], msg: GameMsg) {
    var tupleOption = playerList.find(c => c._1.equalsIgnoreCase(name))
    if (tupleOption != None) {
      tupleOption.get._3 ! msg
    }
  }
}










