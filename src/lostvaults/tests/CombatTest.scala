package lostvaults.tests

import akka.testkit.{ TestFSMRef, TestActorRef }
import akka.actor.{ FSM, ActorSystem }
import scala.concurrent.duration._
import lostvaults.server._
import akka.testkit.{ TestKitBase, TestActorRef, ImplicitSender }
import org.scalatest.FunSuite

class CombatTest
  extends FunSuite
  with TestKitBase
  with ImplicitSender {
  implicit lazy val system = ActorSystem("GroupMapTestSystem")

  test("CombatTest - Addind players that are already in combat") {
    val fsm = TestFSMRef(new Combat)
    val mustBeTypedProperly: TestActorRef[Combat] = fsm

    fsm ! AddPlayer("Anna", 5, "Jimmy")
    fsm ! AddPlayer("Anna", 5, "Jimmy")
    var currentData = fsm.stateData
    var actionData: ActionData = ActionData(List(), List(), 0, List())
    if (currentData.isInstanceOf[RestData]) {
      val restData = currentData.asInstanceOf[RestData]
      actionData = ActionData(restData.PlayerList, List(), restData.Duration, restData.CombatsPerPlayer)
    } else {
      actionData = currentData.asInstanceOf[ActionData]
    }
    //assert(equalsWithoutOrder(actionData.PlayerList, List(("Anna ", 5))))
  }

  test("CombatTest - Adding players and removing") {
    val fsm = TestFSMRef(new Combat)
    val mustBeTypedProperly: TestActorRef[Combat] = fsm

    assert(fsm.stateName == Rest)
    assert(fsm.stateData == RestData(List(), 0, List()))
    fsm ! AddPlayer("Anna", 5, "Jimmy")
    fsm ! AddPlayer("Jimmy", 2, "Anna")
    fsm ! AddPlayer("Fredrik", 7, "Anna")
    fsm ! AddPlayer("Anna", 5, "Fredrik")
    fsm ! AddPlayer("Felix", 8, "Philip")
    fsm ! AddPlayer("Philip", 9, "Felix")
    /*** Takes out the data and changes it to actiondata ***/
    var currentData = fsm.stateData
    var actionData: ActionData = ActionData(List(), List(), 0, List())
    if (currentData.isInstanceOf[RestData]) {
      val restData = currentData.asInstanceOf[RestData]
      actionData = ActionData(restData.PlayerList, List("Anna", "Jimmy", "Felix"), restData.Duration, restData.CombatsPerPlayer)
    } else {
      actionData = currentData.asInstanceOf[ActionData]
    }
    /*******************************************************/
    println("This is the player list: " + actionData.PlayerList)
    assert(TestHelpFunctions.equalsWithoutOrder(actionData.PlayerList, List(("Fredrik", 7), ("Anna", 5), ("Jimmy", 2), ("Felix", 8), ("Philip", 9))))
    assert(TestHelpFunctions.correctEnemies("Anna", "Jimmy", actionData.CombatsPerPlayer))
    assert(TestHelpFunctions.correctEnemies("Anna", "Fredrik", actionData.CombatsPerPlayer))
    assert(TestHelpFunctions.correctLengthEnemyList("Anna", 2, actionData.CombatsPerPlayer))

    fsm.setState(Action, actionData, null, None)
    fsm ! AttackPlayer("Anna", "Jimmy", 2)
    assert(fsm.stateName == WaitForAck)

    fsm ! ActionAck
    assert(fsm.stateName == Action)

    fsm ! AttackPlayer("Jimmy", "Anna", 2)
    assert(fsm.stateName == WaitForAck)

    fsm ! RemovePlayer("Jimmy")
    assert(fsm.stateName == Action)
    actionData = fsm.stateData.asInstanceOf[ActionData]

    assert(actionData.TurnList.length == 1)
    assert(actionData.TurnList.head == "Felix")
    assert(TestHelpFunctions.correctLengthEnemyList("Anna", 1, actionData.CombatsPerPlayer))
    assert(!(actionData.PlayerList.exists(c => c.equals("Jimmy"))))
    assert(TestHelpFunctions.correctEnemies("Anna", "Fredrik", actionData.CombatsPerPlayer))

    fsm ! DrinkPotion("Felix")
    assert(fsm.stateName == Rest)

  }

}