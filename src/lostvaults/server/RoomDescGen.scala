package lostvaults.server

import scala.util.Random

object RoomDescGen {
  var rand = new Random(System.currentTimeMillis)
  //maybe change item listan till en itemlist istället för en stringlist
  def generateDescription(npc: List[String], items: List[String], exits: List[String]) = {
    if (npc.isEmpty && items.isEmpty) {
      var string = "\nThe room you walk into is empty. "
      string += _GenerateExitDesc(exits)
      string += _GenerateMystery
      string
    } else {
      var string = "\n"
      string += _GenerateStartingSentence
      string += _GenerateNPCDesc(npc)
      string += _GenerateItemDesc(items)
      string += _GenerateExitDesc(exits)
      string += _GenerateMystery
      string
    }
  }
  def _GenerateStartingSentence = {
    rand.nextInt(6) match {
      case 0 => {
        "A sudden " + _GenerateSensation + " hits you when you walk into a " + _GenerateAdjective(SIZE) + " " +
          _GenerateRoomType + ". The walls are " + _GenerateAdjective(COLOR) + " with " + _GenerateAdjective(COLOR) +
          " " + _GeneratePattern + " on them. "
      }
      case 1 => {
        "The room you walk into is painted in " + _GenerateAdjective(COLOR) + ". "
      }
      case 2 => {
        "As you walk into the room you hear a " + _GenerateAdjective(SOUND) + " sound ."
      }
      case 3 => {
        "You walk into the " + _GenerateRoomType + " and notice a " + _GenerateAdjective(SIZE) + " " +
          _GenerateFurniture + " in the " + _GenerateCorner + " corner, next to it is a " + " " + _GenerateFurniture + ". "
      }
      case 4 => {
        "You walk into a room. "
      }
      case _ => {
        "The room in front of you is dark and you can hardly see anything, but after a few moments your eyes get used to it and you can see the surroundings. "
      }
    }
  }

  /**
   * @return a list containing all elements from start (including) until end (excluding) in random order
   */
  def _CreateRandomList(start: Int, end: Int, numOfRandom: Int) = {
    var randList: List[Int] = List()
    var array: Array[Int] = new Array(end - start)
    for (i <- 0 until array.length) {
      array(i) = i
    }
    var k = 0
    for (i <- 0 until numOfRandom) {
      k = rand.nextInt(end - start)
      while (array(k) == -1) {
        k = (k + 1) % array.length
      }
      randList = k :: randList
      array(k) = -1
    }
    randList
  }

  def GenerateNPCDesc(npc: List[String]): String = {
    _GenerateNPCDesc(npc)
  }
  def _GenerateNPCDesc(npc: List[String]): String = {

    var randomList = _CreateRandomList(0, 3, rand.nextInt(3) + 1)
    _GenerateOPDesc(npc, randomList, _GenerateNPCDesc)
  }
  def _GenerateOPDesc(npc: List[String], randomList: List[Int], op: (String, Int) => String): String = {
    randomList match {
      case Nil => {
        npc match {
          case Nil =>
            ""
          case _ =>
            op(npc.head, 3) + _GenerateOPDesc(npc.tail, Nil, op)
        }
      }
      case _ => {
        npc match {
          case Nil =>
            ""
          case _ =>
            op(npc.head, randomList.head) + _GenerateOPDesc(npc.tail, randomList.tail, op)
        }
      }
    }
  }
  def _GenerateNPCDesc(npc: String, n: Int): String = {
    n match {
      case 0 => "Asleep on a " + " " + _GenerateFurniture + " is a " + npc + " shining in a " + _GenerateAdjective(COLOR) + " color. "
      case 1 => "In the " + _GenerateCorner + " corner you see a " + _GenerateAdjective(ANYTHING) + " " + npc + ". "
      case 2 => "You look behind you and suddenly there is a " + npc + " there, looking at you with " + _GenerateEmotion + ". "
      case _ => "There is also a " + npc + " in the room. "
    }
  }

  def _GenerateItemDesc(items: List[String]): String = {
    var randomList = _CreateRandomList(0, 3, rand.nextInt(3) + 1)
    _GenerateOPDesc(items, randomList, _GenerateItemDesc)
  }
  def _GenerateItemDesc(item: String, n: Int) = {
    n match {
      case 0 => "In a cavity on the " + _GenerateWall + " wall, you see something " + _GenerateAdjective(COLOR) + ". Upon closer inspection you notice that it is a " + item + ". "
      case 1 => "In front of you on the floor lies a " + item + ". "
      case 2 => "You stumble on something as you walk in, you notice that it is a " + item + ". "
      case _ => "There is also a " + item + " in the room. "
    }
  }
  def _GenerateExitDesc(exits: List[String]) = {
    exits.length match {
      case 0 => "You see no doors in front of you. "
      case 1 => "You see a door to the " + exits.head + ". "
      case 2 => "You see doors to the " + exits.head + " and the" + exits.tail.head + ". "
      case 3 => "You see doors to the " + exits.head + ", " + exits.tail.head + " and to the " + exits.tail.tail.head
      case _ => "You see doors to the " + exits.head + ", " + exits.tail.head + ", " + exits.tail.tail.head + " and to the " + exits.tail.tail.tail.head
    }
  }
  def _GenerateMystery = {
    rand.nextInt(30) match {
      case 0 => "From somewhere you hear a " + _GenerateAdjective(SOUND) + " sound. "
      case 1 => "The room is brightly lit up, but you cannot see a source of light. "
      case 2 => "Something in the room makes you feel a bit dizzy. "
      case 3 => "You feel dripping water on your skin, but when you try to wipe it off you notice there is nothing there. "
      case _ => ""
    }
  }

  def _GenerateCorner = {
    rand.nextInt(3) match {
      case 0 => "south east"
      case 1 => "south west"
      case 2 => "north east"
      case _ => "north west"
    }
  }

  def _GenerateWall = {
    rand.nextInt(3) match {
      case 0 => "south"
      case 1 => "north"
      case 2 => "east"
      case _ => "west"
    }
  }
  def _GenerateFurniture = {
    rand.nextInt(12) match {
      case 0 => "rug"
      case 1 => "chair"
      case 2 => "stone table"
      case 3 => "carpet"
      case 4 => "table"
      case 5 => "bench"
      case 6 => "chest"
      case 7 => "throne"
      case 8 => "stool"
      case 9 => "lion skin"
      case 10 => "stone"
      case _ => "wooden box"
    }
  }
  //Ranges for generateAdjective
  val COLOR = Tuple2(0, 12)
  val ANYTHING = Tuple2(0, 16)
  val SIZE = Tuple2(13, 16)
  val SOUND = Tuple2(17, 22)
  def _GenerateAdjective(range: Tuple2[Int, Int]) = {
    val num = rand.nextInt(range._2) + range._1
    num match {
      case 0 => "lemony yellow"
      case 1 => "blue"
      case 2 => "red"
      case 3 => "brown"
      case 4 => "chrimson"
      case 5 => "golden"
      case 6 => "forrest green"
      case 7 => "light pink"
      case 8 => "greyish black"
      case 9 => "black"
      case 10 => "grey"
      case 11 => "bood colored"
      case 12 => "turqoise"
      case 13 => "huge"
      case 14 => "large"
      case 15 => "small"
      case 16 => "petite"
      case 17 => "squeaking"
      case 18 => "roaring"
      case 19 => "melodious"
      case 20 => "deafening"
      case 21 => "purring"
      case _ => "mumbleing"
    }
  }
  def _GenerateEmotion = {
    rand.nextInt(4) match {
      case 0 => "anger"
      case 1 => "fright"
      case 2 => "sadness"
      case _ => "fury"
    }
  }
  def _GenerateSensation = {
    rand.nextInt(5) match {
      case 0 => "heat"
      case 1 => "cold"
      case 2 => "smell of " + _GenerateSmell
      case 3 => "warmth"
      case _ => "mist"
    }
  }
  def _GenerateRoomType = {
    rand.nextInt(7) match {
      case 0 => "hall"
      case 1 => "staircase room"
      case 2 => "grand hall"
      case _ => "room"
    }
  }
  def _GenerateSmell = {
    rand.nextInt(5) match {
      case 0 => "horses"
      case 1 => "roses"
      case 2 => "water"
      case 3 => "rot"
      case _ => "mold"
    }
  }
  def _GeneratePattern = {
    rand.nextInt(5) match {
      case 0 => "flowers"
      case 1 => "owls"
      case 2 => "frogs"
      case 3 => "strange symbols, that you don't understand,"
      case _ => "lions"
    }
  }
}