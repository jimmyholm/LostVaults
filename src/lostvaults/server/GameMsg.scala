package lostvaults.server
import akka.actor.{ActorRef}
/**
 * Internal messages passed between Player and Dungeon actors
 */
sealed trait GameMsg
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
 * Passed along to a dungeon, this will send a system message to all players in the dungeon passed along to it.
 * @param msg The notification to be sent. 
 */
case class GameNotifyDungeon(msg: String) extends GameMsg
/**
 * Passed along to a dungeon, this will send a system message to the room the sender is in. 
 * @param msg The notification to be sent.
 */
case class GameNotifyRoom(msg: String) extends GameMsg