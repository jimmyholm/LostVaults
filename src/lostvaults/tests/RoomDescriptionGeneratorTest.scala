package lostvaults.tests
import lostvaults.server._
import org.scalatest._

class RoomDescGenTest extends FunSuite {
  test("Just printing a RoomDescription to see if it is nice:") {
	  println(RoomDescGen.generateDescription(List("troll", "skeletton"), List("sword", "necklace"), List("north", "south", "east", "west")))
	  println("\n\n")
	  println(RoomDescGen.generateDescription(List("cat", "person", "owl"), List("box", "armor"), List()))
  }

}