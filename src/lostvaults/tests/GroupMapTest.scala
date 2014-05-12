package lostvaults.tests
import akka.actor.ActorSystem
import akka.testkit.{ TestKitBase, TestActorRef, ImplicitSender }
import org.scalatest.FunSuite
import lostvaults.server._

class GroupMapTest
  extends FunSuite
  with TestKitBase
  with ImplicitSender {
  implicit lazy val system = ActorSystem("GroupMapTestSystem")
  test("Setting up test environment.") {
    
    Main.PMap = Some(TestActorRef[PlayerMap])
  }
  test("Test player list and player count of non-existent group.") {
    val aref = TestActorRef[GroupMap]
    aref ! GMapGetPlayerList("test")
    expectMsg(GMapGetPlayerListResponse(List("test")))
    aref ! GMapGetPlayerCount("test")
    expectMsg(GMapGetPlayerCountResponse(1))
  }
  test("Test adding name to playerlist.") {
    val aref = TestActorRef[GroupMap]
    val testee = aref.underlyingActor
    aref ! GMapJoin("test", "Test2")
    assertResult(true) {
      testee.groupMap.exists((c => c._1 == "test"))
    }
    assertResult(true) {
      testee.groupMap.exists((c => c._1 == "Test2"))
    }
  }
  test("Test of fetching list of players.") {
    val actorRef = TestActorRef[GroupMap]
    actorRef ! GMapJoin("test", "Test2")
    actorRef ! GMapGetPlayerList("test")
    expectMsg(GMapGetPlayerListResponse(List("test", "Test2")))
  }
  test("Test of fetching number of players in a group.") {
    val actorRef = TestActorRef[GroupMap]
    actorRef ! GMapJoin("test", "Test2")
    actorRef ! GMapGetPlayerCount("test")
    expectMsg(GMapGetPlayerCountResponse(2))
  }
  test("Test of leaving a player group.") {
    val actorRef = TestActorRef[GroupMap]
    actorRef ! GMapJoin("test", "Test2")
    val testee = actorRef.underlyingActor
    actorRef ! GMapLeave("test")
    assertResult(false) {
      testee.groupMap.exists((c => c._1 == "test"))
    }
  }
  test("Test of number of players in a group after player leaves.") {
    val actorRef = TestActorRef[GroupMap]
    actorRef ! GMapJoin("test", "Test2")
    actorRef ! GMapLeave("test")
    actorRef ! GMapGetPlayerCount("Test2")
    expectMsg(GMapGetPlayerCountResponse(1))
    actorRef ! GMapGetPlayerCount("test")
    expectMsg(GMapGetPlayerCountResponse(1))
  }
}