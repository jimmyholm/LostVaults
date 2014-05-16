package lostvaults.tests

import org.scalatest.FunSuite
import lostvaults.server.Room

class RoomTest extends FunSuite {
  // implicit lazy val system = ActorSystem("RoomSystem")
  var room = new Room()
  val TestMan = "testman"
  val playerList: List[String] = List("Philip", "Jimmy", "Anna", "Felix", "Fredrik")

  test("This test checks if non existing player is not in room!") {
    assertResult(false) {
      room.hasPlayer(TestMan)
    }
  }

  test("This test checks if added player in playerlist is added!") {
    room.addPlayer(TestMan)
    assert(room.hasPlayer(TestMan))
  }

  test("This test checks if removed player in playerlist is removed!") {
    room.removePlayer(TestMan)
    assert(room.hasPlayer(TestMan))
  }

  test("This test checks the playerlist of a room") {
    room.addPlayer("Jimmy")
    room.addPlayer("Anna")
    room.addPlayer("Philip")
    room.addPlayer("Fredrik")
    room.addPlayer("Felix")
    TestHelpFunctions.equalsWithoutOrder(room.getPlayerList, playerList)
  }

  test("This test checks if a player is in the room") {
    room.addPlayer("Jimmy")
    room.addPlayer("Anna")
    assert(room.hasPlayer("Anna"))
    assert(room.hasPlayer("Jimmy"))                                         
  }
  
  test("This test "){
    
    
  }

  /*	test("This test checks if non existing item is not in room!") {asza<
		assertResult(false) {
			room.hasItem(TestMan)
			}
		}
		
 	test("This test checks if added item in itemlist is added!") {
		room.addItem(TestMan)
			assert(room.hasItem(TestMan))
			}	

	test("This test checks if removed item in itemlist is removed!") {
		room.removeItem(TestMan)
			assert(room.hasItem(TestMan))
			}
			
			test("This test checks if non existing NPC is not in room!") {
		assertResult(false) {
			room.hasNPC(TestMan)
			}
		}
		
 	test("This test checks if added NPC in NPC is added!") {
		room.addNPC(TestMan)
			assert(room.hasNPC(TestMan))
			}	

	test("This test checks if removed NPC in NPClist is removed!") {
		room.removeNPC(TestMan)
			assert(room.hasNPC(TestMan))
			}
			*/
}
