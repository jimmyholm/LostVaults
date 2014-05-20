package lostvaults.tests

object TestHelpFunctions {
  /**
   * @return true if name's enemyList is enemyListLength long, else false
   */
  def correctLengthEnemyList(name: String, enemyListLength: Int, enemyList: List[Tuple2[String, List[String]]]) = {
    (enemyList.filter(c => c._1.equals(name)).length == 1) &&
      (enemyList.filter(c => c._1.equals(name)).head._2.length == enemyListLength)
  }
  /**
   * @return true if enemy1 has enemy2 is in enemy1's enemyList and vice versa, else false
   */
  def correctEnemies(enemy1: String, enemy2: String, enemyList: List[Tuple2[String, List[String]]]) = {
    enemyList.foldRight(false)((c, d) => if (c._1.equals(enemy1)) { d || c._2.contains(enemy2) } else { d || false }) &&
      enemyList.foldRight(false)((c, d) => if (c._1.equals(enemy2)) { d || c._2.contains(enemy1) } else { d || false })
  }
  /**
   * @return true if list1 has exactly the same elements has list2, without order, else false
   */
  def equalsWithoutOrder(list1: List[Any], list2: List[Any]) = {
    (list1.forall(c => list2.contains(c))) &&
      (list2.forall(c => list1.contains(c)))
  }
}