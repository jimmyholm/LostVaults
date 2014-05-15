package lostvaults.server
import scala.collection.mutable.Set
/**
 * PlayerGroup signifies a party of player characters. It's through PlayerGroup entities that players may enter dungeons. Any information to be
 * retrieved from or shared with a PlayerGroup has to go through the GroupMap actor.
 */
class PlayerGroup {
  /**
   * PMap contains a reference to Main.PMap, to maintain the connection between character name and Player actor.
   */
  val PMap = Main.PMap.get
  /**
   * playerSet is a set of players in the current group, stored by their character names and tied to their "ready" state.
   */
  var playerSet: Set[(String, Boolean)] = Set()
  /**
   * groupQuests will contain a list of all group quests, once quests are implemented.
   */
  var groupQuests: Option[Int] = None
  /**
   * playerQuests will link player names with individual subterfuge quests, once quests are implemented.
   */
  var playerQuests: Option[Int] = None
  /**
   * inDungeon is true when a group of players is in a dungeon.
   */
  var inDungeon: Boolean = false
  /**
   * groupSendMessage allows for a Game Message to be sent to each member of the group.
   * @param msg The Game Message to be relayed to every element in the group's playerSet.
   */
  def groupSendMessage(msg: GameMsg) {
    playerSet.foreach((c => PMap ! PMapSendGameMessage(c._1, msg)))
  }

  /**
   * addPlayer adds a player to the group by name, and sends a message that <b>name</b> has joined the party to every other group member.
   * @param name The name of the player to be added to the group
   */
  def addPlayer(name: String) {
    groupSendMessage(GameSystem(name + " joined the party."))
    playerSet += Tuple2(name, false)
  }
  /**
   * removePlayer removes a player from the group by name and sends a message to the remaining players that <b>name</b> has left the party.
   * @param name The name of the player to be removed.
   */
  def removePlayer(name: String) {
    val exists = playerSet.find(c => c._1 == name)
    if (!(exists isEmpty)) {
      playerSet = playerSet.filterNot(c => c._1 == name)
      groupSendMessage(GameSystem(name + " has left the party."))
    }
  }
  /**
   * playerInGroup returns true if the requested player name is part of the player party.
   * @param name The name of the player whose presence is inquired about.
   */
  def playerInGroup(name: String): Boolean = {
    !(playerSet find (c => c._1 == name) isEmpty)
  }
  /**
   * listPlayers returns a list of names of all the players currently in this group.
   */
  def listPlayers(): List[String] = {
    var ret: List[String] = List()
    (playerSet toList).foreach(f => ret = f._1 :: ret)
    ret
  }
  /**
   * playerCount returns the number of players currently in the group.
   */
  def playerCount(): Int = {
    playerSet size
  }
  
  def isGroupReady: Boolean = {
    var ret = 0
    playerSet.foreach(c => if(c._2) ret+=1)
    ret == (playerSet size)
  }
  
  def setReady(name: String) {
    val exists = playerSet.find(c => c._1 == name)
    if (!(exists isEmpty)) {
      removePlayer(name)
      playerSet += Tuple2(name, true)
      groupSendMessage(GameSystem(name + " is ready to enter the dungeon!"))
    }
  }
  def setUnready(name: String) {
    val exists = playerSet.find(c => c._1 == name)
    if (!(exists isEmpty)) {
      removePlayer(name)
      playerSet += Tuple2(name, false)
      groupSendMessage(GameSystem(name + " is no longer ready to enter the dungeon!"))
    }
  }
  def noOneReady():Boolean = {
    var ret = 0
    playerSet.foreach(c => if(c._2) ret+=1)
    ret == 0
  }
}