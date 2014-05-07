package lostvaults.tests

import org.scalatest._
import lostvaults.Parser

class ParserTest extends FunSuite {
  test("Parser.findWord is invoked on \"This is a test!\" with index 0.") {
    assertResult("This") {
      Parser.findWord("This is a test!", 0)
    }
  }
  test("Parser.findWord is invoked on \"This is a test!\" with index 2.") {
    assertResult("a") {
      Parser.findWord("This is a test!", 2)
    }
  }
  test("Parser.findWord is invoked on \"This is a test!\" with index 9.") {
    assertResult("test!") {
      Parser.findWord("This is a test!", 9)
    }
  }
  test("Parser.findRest is invoked on \"This is a test!\" with index 1.") {
    assertResult("a test!") {
      Parser.findRest("This is a test!", 1)
    }
  }
}