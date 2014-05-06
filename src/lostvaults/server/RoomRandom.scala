package lostvaults.server

import scala.util.Random

object RoomRandom {

  def init(Rooms: Array[Room]) = {
    val Random = new Random()
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

    Rooms(StartRoom).isConnected
    Rooms(StartRoom).isCreated
    Rooms(StartRoom).eastConnected
    Rooms(StartRoom).northConnected
    Rooms(StartRoom).westConnected
    Rooms(StartRoom).southConnected

    Rooms(StartRoom - 1).isConnected
    Rooms(StartRoom - 1).isCreated
    Rooms(StartRoom - 1).eastConnected

    Rooms(StartRoom + 1).isConnected
    Rooms(StartRoom + 1).isCreated
    Rooms(StartRoom + 1).westConnected

    Rooms(StartRoom + 10).isConnected
    Rooms(StartRoom + 10).isCreated
    Rooms(StartRoom + 10).southConnected

    Rooms(StartRoom - 10).isConnected
    Rooms(StartRoom - 10).isCreated
    Rooms(StartRoom - 10).northConnected
    
    makeMagic
  }
  
  def makeMagic() = {
    
  }

}