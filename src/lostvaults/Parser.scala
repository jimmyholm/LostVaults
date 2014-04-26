package lostvaults
/** Parser contains functionality to extract a single word from or the "tail" of a
 *  string, in order to parse player-requested actions. 
 */
object Parser {
  /** findWord extracts the ith word from a string, starting at index 0.
   *  @param msg The string to be parsed
   *  @param i The word to be found, with the first word being i = 0
   *  Usage Example: 
   *  findWord("This is a string", 2) = "a"
   *  findWord("This is a string", 1) = "is"
   */
  def findWord(msg: String, i: Int): String = {
    _FindWord(msg, i, 0)
  }
  def _FindWord(msg: String, i: Int, acc: Int): String = {
    val s = msg.indexOfSlice(" ")
    if (s == -1)
      msg
    else
      acc match {
        case `i` => {
          msg.substring(0, msg.indexOfSlice(" "))
        }
        case _ => {

          if (s < msg.length)
            _FindWord(msg.substring(s + 1, msg.length), i, acc + 1)
          else
            msg
        }
      }
  }
  /** findRest extracts the remainder of a string starting from after the from:th
   *  word, beginning at index 0.
   *  @param msg The string to be parsed
   *  @param from The word after which we want to extract the remainder, with the first word being from = 0
   *  Usage Examples:
   *  findRest("This is a string", 0) = "is a string"
   *  findRest("This is a string", 2) = "string"  
   */
  def findRest(msg: String, from: Int): String = {
    _FindRest(msg, from, 0)
  }
  def _FindRest(msg: String, i: Int, acc: Int): String = {
    val s = msg.indexOfSlice(" ")
    if (s == -1)
      msg
    else
      acc match {
        case `i` => {
          msg.substring(s + 1, msg.length)
        }
        case _ => {
          _FindRest(msg.substring(s + 1, msg.length), i, acc + 1)
        }
      }
  }
}