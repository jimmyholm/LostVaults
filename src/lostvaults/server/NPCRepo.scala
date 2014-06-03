package lostvaults.server
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.JdbcBackend
import scala.slick.driver.SQLiteDriver.simple._
import Q.interpolation
import scala.util.Random
import akka.actor.{ ActorRef, Props, Actor, ActorSystem }

object NPCRepo {
  import JdbcBackend.Database
  case class NPCData(id: Int, name: String, minhp: Int, maxhp: Int, rating: Int)
  implicit val getNPCDataResult = GetResult(r => NPCData(r.nextInt, r.nextString, r.nextInt, r.nextInt, r.nextInt))
  var random = new Random(System.currentTimeMillis)
  var npcList: List[NPCData] = List()
  def populateArray {
    Database.forURL("jdbc:sqlite:lostvaults.db", driver = "org.sqlite.JDBC") withSession {
      implicit session =>
        Q.queryNA("SELECT * FROM NPC") foreach (c => npcList = npcList :+ c)
    }
  }
  populateArray
  def rand(min: Int, max: Int) = {
    min + (random.nextFloat() * (max - min).asInstanceOf[Float]).asInstanceOf[Int]
  }
  def getRandomNPC(system: ActorSystem, dungeon: ActorRef, rating: Int, room: Int): (String, ActorRef) = {
    val ratingList = npcList.filter(c => c.rating <= rating)
    val n = random.nextInt(ratingList.length)
    val newNPCData = ratingList.apply(n)
    var hp = rand(newNPCData.minhp, newNPCData.maxhp)
    (newNPCData.name, system.actorOf(NPC.props(newNPCData.name, hp, newNPCData.rating, dungeon, room)))
  }
  def getManyRandom(num: Int, system: ActorSystem, dungeon: ActorRef, rating: Int, room: Int): List[(String, ActorRef)] = {
    var list: List[(String, ActorRef)] = List()
    for (x <- 0 until num) {
      list = getRandomNPC(system, dungeon, rating, room) :: list
    }
    list
  }
}

