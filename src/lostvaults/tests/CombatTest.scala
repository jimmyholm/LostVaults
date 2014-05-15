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

  test("Testing Combat - Adding players and removing") {
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
    assert(equalsWithoutOrder(actionData.PlayerList, List(("Fredrik", 7), ("Anna", 5), ("Jimmy", 2), ("Felix", 8), ("Philip", 9))))
    assert(correctEnemies("Anna", "Jimmy", actionData.CombatsPerPlayer))
    assert(correctEnemies("Anna", "Fredrik", actionData.CombatsPerPlayer))
    assert(correctLengthEnemyList("Anna", 2, actionData.CombatsPerPlayer))

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
    assert(correctLengthEnemyList("Anna", 1, actionData.CombatsPerPlayer))
    assert(!(actionData.PlayerList.exists(c => c.equals("Jimmy"))))
    assert(correctEnemies("Anna", "Fredrik", actionData.CombatsPerPlayer))
    
    fsm ! DrinkPotion("Felix")
    assert(fsm.stateName == Rest)

    def correctLengthEnemyList(name: String, enemyListLength: Int, enemyList: List[Tuple2[String, List[String]]]) = {
      (enemyList.filter(c => c._1.equals(name)).length == 1) &&
        (enemyList.filter(c => c._1.equals(name)).head._2.length == enemyListLength)
    }
    def correctEnemies(enemy1: String, enemy2: String, enemyList: List[Tuple2[String, List[String]]]) = {
      enemyList.foldRight(false)((c, d) => if (c._1.equals(enemy1)) { d || c._2.contains(enemy2) } else { d || false }) &&
        enemyList.foldRight(false)((c, d) => if (c._1.equals(enemy2)) { d || c._2.contains(enemy1) } else { d || false })
    }
    def equalsWithoutOrder(list1: List[Any], list2: List[Any]) = {
      (list1.forall(c => list2.contains(c))) &&
        (list2.forall(c => list1.contains(c)))
    }
  }
}