package lostvaults.server

import scala.util.Random

/**
 * 
 * 
 */
object RoomRandom {
  val Random = new Random()


  /**
   * This method creates a start room with four possible directions to go.
   * @param Rooms An array of rooms.
   * 
   */
  def init(Rooms: Array[Room]) = {
    var StartRoom = Random.nextInt(89)
    if (StartRoom < 10) {
      StartRoom += 10
    }
    if ((StartRoom % 10) == 0) {
      StartRoom + 1
    }
    val StartString = StartRoom.toString
    if (StartString.contains("9")) {
      StartRoom - 1
    }

    Rooms(StartRoom).setConnected
    Rooms(StartRoom).setCreated
    Rooms(StartRoom).eastConnected
    Rooms(StartRoom).northConnected
    Rooms(StartRoom).westConnected
    Rooms(StartRoom).southConnected

    Rooms(StartRoom - 1).setConnected
    Rooms(StartRoom - 1).setCreated
    Rooms(StartRoom - 1).eastConnected

    Rooms(StartRoom + 1).setConnected
    Rooms(StartRoom + 1).setCreated
    Rooms(StartRoom + 1).westConnected

    Rooms(StartRoom + 10).setConnected
    Rooms(StartRoom + 10).setCreated
    Rooms(StartRoom + 10).southConnected

    Rooms(StartRoom - 10).setConnected
    Rooms(StartRoom - 10).setCreated
    Rooms(StartRoom - 10).northConnected
    
    makeMagic
  }
  /**
   * This method makes magic.
   */
  def makeMagic() = {
    
    
    
  }

}