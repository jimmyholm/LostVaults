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
    (min + (random.nextFloat() * (max - min))).asInstanceOf[Int]
  }
  def getRandomNPC(system: ActorSystem, rating: Int): (String, ActorRef) = {
    println("GetRand HEllo1")

    val ratingList = npcList.filter(c => c.rating <= rating)
    println("GetRand HEllo2")
    val n = random.nextInt(ratingList.length)
    println("GetRand HEllo3")
    val newNPCData = ratingList.apply(n)
    println("GetRand HEllo4")
    var hp = rand(newNPCData.minhp, newNPCData.maxhp)
    println("GetRand HEllo5")
    (newNPCData.name, system.actorOf(NPC.props(newNPCData.name, hp, newNPCData.rating)))
  }
  def getManyRandom(num: Int, system: ActorSystem, rating: Int): List[(String, ActorRef)] = {
    println("GetMany Hello1")
    var list: List[(String, ActorRef)] = List()
    println("GetMany Hello2")
    for (x <- 0 until num) {
      println("GetMany Hello3")
      list = getRandomNPC(system, rating) :: list
    }
    println("GetMany Hello4")
    list
  }
}

