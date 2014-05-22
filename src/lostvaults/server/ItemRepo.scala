package lostvaults.server
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.JdbcBackend
import scala.slick.driver.SQLiteDriver.simple._
import Q.interpolation
import scala.util.Random

object ItemRepo {
  import JdbcBackend.Database
  case class ItemData(id: Int, name: String, attack: Int, defense: Int, speed: Int, rating: Int, itemType: String)
  implicit val getItemDataResult = GetResult(r => ItemData(r.nextInt, r.nextString, r.nextInt,  r.nextInt, r.nextInt, r.nextInt, r.nextString))
  val Rnd = new Random(System.currentTimeMillis)
  var itemArray: Array[ItemData] = Array()
  def populateArray() {
    Database.forURL("jdbc:sqlite:lostvaults.db", driver = "org.sqlite.JDBC") withSession {
      implicit session =>
        Q.queryNA("SELECT * FROM Items") foreach (c => itemArray = itemArray :+ c)
    }
  }
  def getOneRandom(Type: String, Rating: Int): Item = {
    var pool = Array[ItemData]()
    itemArray foreach (item => if ((item.itemType.compareToIgnoreCase(Type) == 0 || Type.compareToIgnoreCase("Any") == 0 ||
      (Type.compareToIgnoreCase("NoTreasure") == 0 && item.itemType.compareToIgnoreCase("Treasure") != 0)) && item.rating <= Rating) pool = pool :+ item)
    val rndHigh = pool.length
    if (rndHigh > 0) {
      val r = (Rnd.nextFloat() * rndHigh - 1).asInstanceOf[Int]
      val itemData = pool(r)
      new Item(itemData.id, itemData.name, itemData.attack, itemData.defense, itemData.speed, itemData.itemType)
    }
    new Item(-4, "Invalid Item", 0, 0, 0, "Invalid")
  }

  def getManyRandom(Amnt: Int, Type: String, Rating: Int): Array[Item] = {
    var pool = Array[ItemData]()
    var ret = Array[Item]()
    itemArray foreach (item => if ((item.itemType.compareToIgnoreCase(Type) == 0 || Type.compareToIgnoreCase("Any") == 0 ||
      (Type.compareToIgnoreCase("NoTreasure") == 0 && item.itemType.compareToIgnoreCase("Treasure") != 0)) && item.rating <= Rating) pool = pool :+ item)
    val rndHigh = pool.length
    var r = 0
    if (rndHigh > 0) {
      for (i <- 0 until Amnt) {
        r = (Rnd.nextFloat() * rndHigh - 1).asInstanceOf[Int]
        val itemData = pool(r)
        if (ret.find(i => i.name.compareToIgnoreCase(itemData.name) == 0) == None)
          ret = ret :+ new Item(itemData.id, itemData.name, itemData.attack, itemData.defense, itemData.speed, itemData.itemType)
      }
    }
    ret
  }

  def getAll(Type: String, Rating: Int): Array[ItemData] = {
    var ret = Array[ItemData]()
    itemArray foreach (item => if (item.itemType.compareToIgnoreCase(Type) == 0 && item.rating <= Rating) ret = ret :+ item)
    ret
  }
  def getAllByType(Type: String): Array[ItemData] = {
    var ret = Array[ItemData]()
    itemArray foreach (item => if (item.itemType.compareToIgnoreCase(Type) == 0) ret = ret :+ item)
    ret
  }
  def getAllByRating(Rating: Int): Array[ItemData] = {
    var ret = Array[ItemData]()
    itemArray foreach (item => if (item.rating <= Rating) ret = ret :+ item)
    ret
  }
  def getById(ID: Int): Item = {
    val itemOp = itemArray.find(item => item.id == ID)
    if (itemOp == None)
      new Item(-4, "Invalid Item", 0, 0, 0, "Invalid")
    else {
      val item = itemOp.get
      val ret: Item = new Item(item.id, item.name, item.attack, item.defense, item.speed, item.itemType)
      ret
    }
  }
  def getItem(Index: Int): Item = {
    var item = itemArray(0)
    if (Index < 0 || Index >= itemArray.length)
      new Item(-4, "Invalid Item", 0, 0, 0, "Invalid")
    item = itemArray(Index)
    val ret: Item = new Item(item.id, item.name, item.attack, item.defense, item.speed, item.itemType)
    ret
  }
}