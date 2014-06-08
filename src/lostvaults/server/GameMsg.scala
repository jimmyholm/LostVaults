
package lostvaults.server
import akka.actor.{ActorRef}
/**
 * Internal messages passed between Player and Dungeon actors
 */
sealed trait GameMsg

/**
 * Passed along to a player, general message that has to follow the form of the client receive 
 */
case class GameMessage(msg: String) extends GameMsg
/**
 * Passed along to a dungeon, it will cause msg to be sent to every player in the
 * same room as the player named name. Passed along to a player, it will send
 * the message "Say name msg" to the remote client.
 * @param name The name of the player speaking
 * @param msg The message sent by the player.
 */
case class GameSay(name: String, msg: String) extends GameMsg
/**
 * Unused 
 */
case class GameWhisper(from: String, to: String, msg: String) extends GameMsg
/**
 * Passed along to a dungeon, it will cause msg to be sent to every player in the
 * same dungeon as the player named name. Passed along to a player, it will send
 * the message "Yell name msg" to the remote client.
 * @param name The name of the player yelling
 * @param msg The message sent by the player.
 */
case class GameYell(name: String, msg: String) extends GameMsg
/**
 * Passed along to a dungeon, the player named name will be removed from the internal
 * player set and will cause a GamePlayerLeft message to be sent to all remaining
 * players in the dungeon.
 * @param name The name of the player that left the dungeon
 */
case class GameRemovePlayer(name: String) extends GameMsg
/**
 * Passed along to a dungeon, the player named name will be added to the internal
 * player set and will cause a GamePlayerEnter message to be sent to all other
 * players in the dungeons.
 * @param name The name of the player that entered the dungeon.
 */
case class GameAddPlayer(name: String) extends GameMsg
/**
 * Passed along to a player, the message "System name has left the dungeon" to the
 * remote client.
 * @param name The name of the player that left the dungeon
 */
case class GamePlayerLeft(name: String) extends GameMsg
/**
 * Passed along to a player, the message "System name has entered the dungeon" to
 * the remote client.
 * @param name The name of the player that entered the dungeon.
 */
case class GamePlayerEnter(name: String) extends GameMsg
/**
 * Passed along to a player, this will update the player's dungeon actor.
 * @param dungeon The ActorRef of the dungeon the player has been moved to.
 */
case class GameMoveToDungeon(dungeon: ActorRef) extends GameMsg
/**
 * Passed along to a player, this will send the message "SYSTEM msg" to the 
 * remote connection.
 * @param msg The system message to be sent to the client. 
 */
case class GameSystem(msg: String) extends GameMsg
/**
 * Passed along to a player when it is that player's turn in combat
 */
case object GameYourTurn extends GameMsg
/**
 * Passed along to a player, tells how much damage that player has suffered
 * @param damage The amount of damage the player has suffered
 * @param from Who has afflicted the damage on the player
 */
case class GameDamage(from: String, damage: Int) extends GameMsg
/**
 * Passed along to a combat, when a player has died
 * @param player The player that is dead
 */
case class GamePlayerHasDied(player: String) extends GameMsg
/**
 * Passed along to a dungeon, this will send a system message to all players in the dungeon passed along to it.
 * @param msg The notification to be sent. 
 */
case class GameNotifyDungeon(msg: String) extends GameMsg
/**
 * Passed along to a dungeon, this will send a system message to room.
 * @param room Index of the room to be notified. 
 * @param msg The notification to be sent.
 */
case class GameNotifyRoom(room: Int, msg: String) extends GameMsg
/**
 * Passed along to a dungeon, this will send a system message to the room the sender is in.
 * @param name The name of the player whose room is to be notified. 
 * @param msg The notification to be sent.
 */
case class GameNotifyRoomByName(name: String, msg: String) extends GameMsg
/**
 * Passed along to a dungeon, this will start a combat between two players, 
 * or continue a combat in action
 * @param attacker The player that is performing the attack
 * @param attackee The player that is being attacked 
 */
case class GameAttackPlayer(attacker: String, attackee: String) extends GameMsg
/**
 * Passed along to a player, when that player is trying to attack someone that is not in room
 * @param name The name of the player being attacked.
 */
case class GameAttackNotInRoom(name: String) extends GameMsg
/**
 * Passed along to a room, when the message sender is trying to attack attackee
 * @param attackee The player that is being attacked
 */
case class GameAttackPlayerInCombat(attackee: String) extends GameMsg
/**
 * Passed along to a player, when that player is being attacked and will 
 * lead to that player being added to a combat
 * @param battle The actor reference to the battle the player should join
 * @param enemy The name of the player under attack
 */
case class GamePlayerJoinBattle(battle: ActorRef, enemy: String) extends GameMsg
/**
 * Passed along to a player, telling that player that it can drink its potion
 */
case object GameDrinkPotion extends GameMsg
/**
 * Passed along to a dungeon, notifying that a battle has ended
 */
case object GameCombatOver extends GameMsg
/**
 * Passed along to a player when that player has won a battle
 */
case object GameCombatWin extends GameMsg

/**
 * Passed along to the room, notifying that room that the battle has ended
 * @param room The index of the room the combat is in
 */
case class GameCombatFinished(room: Int) extends GameMsg
/**
 * Passed along to a player to tell the player to move to a room.
 * @param room The room coordinates to move to
 * @param start True if the player is moved into the starting room.
 */
case class GameDungeonMove(room: Int, start:Boolean) extends GameMsg
/**
 * Passed along to a dungeon to request for a player to move into a new room.
 * @param direction The direction to move
 * @param name The player who wishes to move
 */
case class GamePlayerMove(name: String, direction: Int, index: Int) extends GameMsg

/**
 * Request from a player to exit the dungeon.
 * @param name The name of the player who wishes to leave.
 */
case class GameExitDungeon(name: String) extends GameMsg
/**
 * Request from a player to enter a dungeon
 * @param group Name of a player in the group that wishes to enter.
 */
case class GameEnterDungeon(group: String) extends GameMsg
/**
 * Passed along to Dungeon to inform the Dungeon that a player
 * wishes to pick up an item.
 * @param item The name of the item to pick up
 * @param currentWep The ID of the player's current weapon
 * @param currentArmor The ID of the player's current armor
 * @param player The player who wishes to pick up item
 * @param room The player's current room
 */
case class GamePickUpItem(item: String, currentWep: Int, currentArmor: Int, player: String, room: Int) extends GameMsg
/**
 * Passed along to a Dungeon to inform the Dungeon that a player
 * wishes to drop an item.
 * @param item The item to drop
 * @param room The player's current room
 */
case class GameDropItem(item: Item, room: Int) extends GameMsg
/**
 * Passed along to a Player to inform the player that some item 
 * needs to be updated.
 * @param item The player's new item.
 */
case class GameUpdateItem(item: Item) extends GameMsg

/**
 * Passed along to a dungeon, it relays a network message to all players 
 * in a given room.
 * @param room The index of the relevant room
 * @param msg The network message to be relayed.
 */
case class GameNotifyGUI(room: Int, msg: String) extends GameMsg

/**
 * Message sent to a player to restore the player's health.
 * Sent by the admin tool CheatActor.
 */
case object GameHeal extends GameMsg

/**
 * Message sent to a player to reduce the player's health to a minimum of 1.
 * Sent by the admin tool CheatActor
 * @param amnt The amount to reduce health by.
 */
case class GameHarm(amnt: Int) extends GameMsg
/**
 * Passed along to a Dungeon, when the NPC has died
 * @param name The name of the NPC
 * @param room The room the NPC is in
 */
case class GameRemoveNPCFromRoom(name: String, room: Int) extends GameMsg

/**
 * Passed along to a player, it changes the player's internal combat target.
 * @param newTarget The name of the new target.
 */
case class GamePlayerSetTarget(newTarget: String) extends GameMsg

/** Passed to a dungeon, it removes a player from a room's player set.
 *  @param name The name of the player to be removed
 *  @roomIndex The index of the room to remove the player from.
 */
case class GameRemoveFromRoom(name:String, roomIndex: Int) extends GameMsg
