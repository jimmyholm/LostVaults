package lostvaults.tests
import akka.actor.ActorSystem
import akka.testkit.{ TestKitBase, TestActorRef, ImplicitSender}
import org.scalatest.FunSuite
import lostvaults.server._

class PlayerMapTest
  extends FunSuite 
  with TestKitBase 
  with ImplicitSender{
  val actorRef = TestActorRef[PlayerMap]
  implicit lazy val system = ActorSystem("PlayerMapTestSystem")
  test("IsOnline with empty playerMap.") {
    actorRef ! PMapIsOnline("Noone", "")
    expectMsg(PMapIsOnlineResponse(false, ""))
  }
  test("Adding a non-existent player to the playerMap") {
    actorRef ! PMapAddPlayer("TestMan", actorRef)
    expectMsg(PMapSuccess)
  }
  test("Adding an already existent player to the playerMap") {
    actorRef ! PMapAddPlayer("TestMan", actorRef)
    expectMsg(PMapFailure)
  }
  test("Requesting an existing player.") {
    actorRef ! PMapGetPlayer("TestMan", "")
    expectMsg(PMapGetPlayerResponse(Some(actorRef), ""))
  }
  test("Requesting a non-existent player.") {
    actorRef ! PMapGetPlayer("Noone", "")
    expectMsg(PMapGetPlayerResponse(None, ""))
  }
}