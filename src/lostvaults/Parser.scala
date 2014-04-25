package lostvaults

object Parser {
  def FindWord(msg: String, i: Int): String = {
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
  def FindRest(msg: String, from: Int): String = {
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