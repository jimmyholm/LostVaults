package lostvaults.server
import akka.actor.{ Actor, ActorRef, Props }
import scala.collection.mutable.HashMap
/**
 * GMapMsg serves as the base trait for all GroupMap related messages.
 */
sealed trait GMapMsg

/**
 * GMapJoin is a request to add one player to the group of another. If the player we wish to join is not currently in a group,
 * a new group is created for both players.
 * @param joinee The name of the player who wishes to join a new group.
 * @param group The name of the player whose group the joinee wishes to join.
 */
case class GMapJoin(joinee: String, group: String) extends GMapMsg

/**
 * GMapLeave is a request to remove a player from his current group. Afterwards, the player who wishes to leave is no longer part of
 * any group.
 * @param name The name of the player who wishes to leave their current group.
 */
case class GMapLeave(name: String) extends GMapMsg

/**
 * GMapSendGameMessage is a request to send a game message to the group of a named player. If the player is not in a group, the message is sent
 * only to the player whose name is provided.
 * @param name The name of the player whose group should receive a message.
 * @param msg The Game Message to be relayed to the group.
 */
case class GMapSendGameMessage(name: String, msg: GameMsg) extends GMapMsg

/**
 * GMapGetPlayerList is a request to retrieve a list of players in a given player's group. If the player is not in a group, a list containing
 * only the requested player's name is returned. This request returns a GMapGetPlayerListResposne message to the sender.
 * @param name The name of the player whose group's player list is to be returned.
 */
case class GMapGetPlayerList(name: String) extends GMapMsg

/**
 * GMapGetPlayerListResponse is a response to a GetPlayerList request.
 * @param list A list of string containing the player names in the requested group.
 */
case class GMapGetPlayerListResponse(list: List[String]) extends GMapMsg

/**
 * GMapGetPlayerCount is a request to retreive the number of players currently in a group. The message GMapGetPlayerCountResponse is returned to sender
 * @param name The name of a player whose group player count we wish to find out.
 */
case class GMapGetPlayerCount(name: String) extends GMapMsg

/**
 * GMapGetPlayerCountResponse is a response to a GetPlayerCount request.
 * @param count The number of players in the requested group.
 */
case class GMapGetPlayerCountResponse(count: Int) extends GMapMsg

/**
 * GMapEnterDungeon is sent by the dungeon to the group manager to set a player as ready to enter the dungeon.
 * When all players in the group are ready to enter, the group manager will create a new dungeon and move the players.
 * @param name: The name of the player who is ready to enter the dungeon.
 */
case class GMapEnterDungeon(name: String) extends GMapMsg
/**
 * GMapExitDungeon is sent by a dungeon when a player decides to exit the dungeon they are in. They are then set to unready and
 * may not re-enter the dungeon until all players have left.
 * @param name: The name of the player who is ready to enter the dungeon.
 */
case class GMapExitDungeon(name: String) extends GMapMsg

class GroupMap extends Actor {
  val PMap = Main.PMap.get
  var groupMap: HashMap[String, PlayerGroup] = HashMap()
  def _FindName(name: String): Option[PlayerGroup] = {
    val e = groupMap.find(c => c._1.compareToIgnoreCase(name) == 0)
    if (e isEmpty)
      None
    else
      Some(e.get._2)
  }
  def receive = {
    case GMapJoin(joinee, group) => {
      println("Joining " + joinee + " to " + group)
      val groupOp = _FindName(group)
      val oldGroup = _FindName(joinee)
      if (!groupOp.isEmpty) {
        if (!groupOp.get.playerInGroup(joinee)) {
          if (groupOp.get.inDungeon == false) {
            if (!oldGroup.isEmpty) {
              oldGroup.get.removePlayer(joinee)
              oldGroup.get.groupSendMessage(GameSystem(joinee + "has left the group"))
              groupMap -= joinee
            }
            groupOp.get.groupSendMessage(GameSystem(joinee + "has joined the group"))
            groupMap += Tuple2(joinee, groupOp.get)
            groupOp.get.addPlayer(joinee)
          } else {
            PMap ! PMapSendGameMessage(joinee, GameSystem("You can not join a group of players currently in a dungeon."))
          }
        }
      } else {
        var join = new PlayerGroup
        if (!oldGroup.isEmpty) {
          oldGroup.get.removePlayer(joinee)
          groupMap -= joinee
          oldGroup.get.groupSendMessage(GameSystem(joinee + "has left the group"))
        }
        join.addPlayer(joinee)
        join.addPlayer(group)
        groupMap += Tuple2(joinee, join)
        groupMap += Tuple2(group, join)
        PMap ! PMapSendGameMessage(group, GameSystem("You have formed a group with " + joinee))
        PMap ! PMapSendGameMessage(joinee, GameSystem("You have formed a group with " + group))
      }
    }
    case GMapLeave(name) => {
      val groupOp = _FindName(name)
      if (!groupOp.isEmpty) {
        groupOp.get.removePlayer(name)
        groupMap -= name
        groupOp.get.groupSendMessage(GameSystem(name + " has left the group."))
      }
    }
    case GMapSendGameMessage(name, msg) => {
      val groupOp = _FindName(name)
      if (!groupOp.isEmpty) {
        groupOp.get.groupSendMessage(msg)
      } else {
        PMap ! PMapSendGameMessage(name, msg)
      }
    }
    case GMapGetPlayerList(name) => {
      val groupOp = _FindName(name)
      if (!groupOp.isEmpty) {
        sender ! GMapGetPlayerListResponse(groupOp.get.listPlayers)
      } else {
        sender ! GMapGetPlayerListResponse(List(name))
      }
    }
    case GMapGetPlayerCount(name) => {
      val groupOp = _FindName(name)
      if (!groupOp.isEmpty) {
        sender ! GMapGetPlayerCountResponse(groupOp.get.playerCount)
      }
      sender ! GMapGetPlayerCountResponse(1)
    }
    case GMapEnterDungeon(name) => {
      val groupOp = _FindName(name)
      if (groupOp.isEmpty) { // Player is not in a group.
        var join = new PlayerGroup
        join.addPlayer(name)
        PMap ! PMapSendGameMessage(name, GameSystem("Entering dungeon..."))
        PMap ! PMapSendGameMessage(name, GameMessage("GUIDUNGEON"))
        val newDungeon = context.actorOf(Props[Dungeon])
        newDungeon ! NewDungeon
        newDungeon ! GameAddPlayer(name)
        groupMap(name) = join
      } else {
        var group = groupOp.get
        group.setReady(name)
        if (group isGroupReady) {
          group.groupSendMessage(GameSystem("All players ready! Entering dungeon..."))
          PMap ! PMapSendGameMessage(name, GameMessage("GUIDUNGEON"))
          val newDungeon = context.actorOf(Props[Dungeon])
          newDungeon ! NewDungeon
          val list = group.listPlayers
          println("Putting players " + list + " into new dungeon.")
          list.foreach(n => { groupMap(n) = group; newDungeon ! GameAddPlayer(n) })
        } else {
          group.groupSendMessage(GameSystem(name + " is ready to enter the dungeon!"))
        }
      }
    }
    case GMapExitDungeon(name) => {
      val groupOp = _FindName(name)
      if (!groupOp.isEmpty) { // Only do something if the player who wishes to exit is actually in a group.
        var group = groupOp.get
        group.setUnready(name)
        if (group.inDungeon == true) {
          group.groupSendMessage(GameSystem(name + " has left the dungeon!"))
        } else {
          group.groupSendMessage(GameSystem(name + " is no longer ready to enter the dungeon."))
        }
        val list = group.listPlayers
        if (group.noOneReady)
          group.inDungeon = false
        list.foreach(n => groupMap(n) = group)
      }
    }
  }
}