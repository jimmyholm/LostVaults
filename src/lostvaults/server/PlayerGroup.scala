package lostvaults.server
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
   * playerSet is a set of players in the current group, stored by their character names.
   */
  var playerSet: Set[String] = Set()
  /**
   * groupQuests will contain a list of all group quests, once quests are implemented.
   */
  var groupQuests: Option[Int] = None
  /**
   * playerQuests will link player names with individual subterfuge quests, once quests are implemented.
   */
  var playerQuests: Option[Int] = None

  /**
   * groupSendMessage allows for a Game Message to be sent to each member of the group.
   * @param msg The Game Message to be relayed to every element in the group's playerSet.
   */
  def groupSendMessage(msg: GameMsg) {
    playerSet.foreach((c => PMap ! PMapSendGameMessage(c, msg)))
  }

  /**
   * addPlayer adds a player to the group by name, and sends a message that <b>name</b> has joined the party to every other group member.
   * @param name The name of the player to be added to the group
   */
  def addPlayer(name: String) {
    groupSendMessage(GameSystem(name + " joined the party."))
    playerSet += name
  }
  /**
   * removePlayer removes a player from the group by name and sends a message to the remaining players that <b>name</b> has left the party.
   * @param name The name of the player to be removed.
   */
  def removePlayer(name: String) {
    val exists = playerSet.find(c => c == name)
    if (!(exists isEmpty)) {
      playerSet -= name
      groupSendMessage(GameSystem(name + " has left the party."))
    }
  }
  /**
   * playerInGroup returns true if the requested player name is part of the player party. 
   * @param name The name of the player whose presence is inquired about. 
   */
  def playerInGroup(name: String): Boolean = {
    !(playerSet find(c => c == name) isEmpty)
  }
  /**
   * listPlayers returns a list of names of all the players currently in this group.
   */
  def listPlayers(): List[String] = {
    playerSet toList
  }
  /** playerCount returns the number of players currently in the group.
   */
  def playerCount(): Int = {
    playerSet size
  }
}