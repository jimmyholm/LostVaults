/**
 * CheatActor.scala
 * @Author Felix Färsjö, Jimmy Holm, Fredrik Larsson, Anna Nilsson, Philip Åkerfeldt
 * @Version 1.0
 */


package lostvaults.server

import lostvaults.Parser
import akka.event.Logging
import akka.actor.{ Actor, ActorRef }
import akka.util.ByteString
import akka.io.{ Tcp }
import scala.util.Random
import scala.collection.mutable.Queue
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.JdbcBackend
import scala.slick.driver.SQLiteDriver.simple._
import Q.interpolation

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
/**
 * CheatActor provides a way to perform admin-related tasks on the server, including testing functionality
 * such as giving items to player, healing players, hurting players and listing NPCs and Items from the
 * database.
 */
class CheatActor extends Actor {
  val PMap = Main.PMap.get
  val log = Logging(context.system, this)
  def receive = {
    case m: String => {
      Parser.findWord(m.toLowerCase, 0) match {
        case "give" => { // Syntax: "Give id player" or "give potion amount player" or "give food amount player" 
          try {
            var id = 0
            var name = ""
            var item = ItemRepo.getById(-4)
            if (Parser.findWord(m, 1).compareToIgnoreCase("Potion") == 0 ||
              Parser.findWord(m, 1).compareToIgnoreCase("Food") == 0) {
              println("Giving consumable")
              id = Parser.findWord(m, 2).toInt
              item = new Item(-1, "Food", id, 0, 0, "Food")
              name = Parser.findRest(m, 2)
            } else {
              println("Giving item")
              id = Parser.findWord(m, 1).toInt
              name = Parser.findRest(m, 1)
              item = ItemRepo.getById(id)
            }
            if (item.id == -4) { // Invalid item.
              println("Failed to give item, item id does not exist.")
            } else {
              implicit val timeout = Timeout(5.seconds)
              val future = PMap ? PMapIsOnline(name, "")
              val result = Await.result(future, timeout.duration).asInstanceOf[PMapIsOnlineResponse]
              if (result.online) { // Player is online, give them the stuff.
                PMap ! PMapSendGameMessage(name, GameUpdateItem(item))
                println("Gave " + item.name + " to player \"" + name + "\".")
              } else {
                println("Player " + name + " is not online.")
              }
            }
          } catch {
            case e: NumberFormatException => {
              log.error("CheatActor: Can't execute \"" + m + "\"invalid id.\n" + e.getMessage())
            }
          }
        }
        case "heal" => { // heal player
          val name = Parser.findRest(m, 0)
          implicit val timeout = Timeout(5.seconds)
          val future = PMap ? PMapIsOnline(name, "")
          val result = Await.result(future, timeout.duration).asInstanceOf[PMapIsOnlineResponse]
          if (result.online) { // Player is online, give them the stuff.
            PMap ! PMapSendGameMessage(name, GameHeal)
            println("Healed player " + name + ".")
          } else {
            println("Player " + name + " is not online.")
          }
        }
        case "harm" => { // harm amount player
          try {
            val name = Parser.findRest(m, 1)
            val amnt = Parser.findWord(m, 1).toInt
            implicit val timeout = Timeout(5.seconds)
            val future = PMap ? PMapIsOnline(name, "")
            val result = Await.result(future, timeout.duration).asInstanceOf[PMapIsOnlineResponse]
            if (result.online) { // Player is online, give them the stuff.
              PMap ! PMapSendGameMessage(name, GameHarm(amnt))
              println("Harmed player " + name + " by " + amnt + " points.")
            } else {
              println("Player " + name + " is not online.")
            }
          } catch {
            case _: NumberFormatException => {
              println("Failed to harm player, amount not a number.")
            }
          }
        }
        case "find" => { // find item name, find all item ids which contain a given string. find npc name, find all npc ids which contain 
          // a given string.
          val name = Parser.findRest(m, 1)
          if (Parser.findWord(m, 1).compareTo("item") == 0) { // Find items
            case class ItemData(id: Int, name: String, attack: Int, defense: Int, speed: Int, rating: Int, itemType: String)
            implicit val getItemDataResult = GetResult(r => ItemData(r.nextInt, r.nextString, r.nextInt, r.nextInt, r.nextInt, r.nextInt, r.nextString))
            Database.forURL("jdbc:sqlite:lostvaults.db", driver = "org.sqlite.JDBC") withSession {
              implicit session =>
                val sql = "SELECT * FROM ITEMS WHERE name LIKE '%" + name + "%'"
                println("Listing all items containing \"" + name + "\":")
                Q.queryNA[ItemData](sql) foreach (i => println("ID: " + i.id + "\tName: " + i.name + (if(i.name.length < 15)"\t" else if(i.name.length < 20)"\t\t"else"\t") + "Atk: " + i.attack + "\tDef: " + i.defense+ "\tSpd: " + i.speed))
                println("Finished.")
            }
          } else if (Parser.findWord(m, 1).compareTo("npc") == 0) { // Find npcs
            case class NpcData(id: Int, name: String, minhp: Int, maxhp: Int, rating: Int)
            implicit val getNpcDataResult = GetResult(r => NpcData(r.nextInt, r.nextString, r.nextInt, r.nextInt, r.nextInt))
            Database.forURL("jdbc:sqlite:lostvaults.db", driver = "org.sqlite.JDBC") withSession {
              implicit session =>
                val sql = "SELECT * FROM NPC WHERE name LIKE '%" + name + "%'"
                println("Listing all NPCs containing \"" + name + "\":")
                Q.queryNA[NpcData](sql) foreach (i => println("ID: " + i.id + "\tName: " + i.name))
                println("Finished.")
            }
          }
        }
        case "tell" => { // tell player message
          val name = Parser.findWord(m, 1)
          val msg = Parser.findRest(m, 1)
          implicit val timeout = Timeout(5.seconds)
          val future = PMap ? PMapIsOnline(name, "")
          val result = Await.result(future, timeout.duration).asInstanceOf[PMapIsOnlineResponse]
          if (result.online) { // Player is online, give them the stuff.
            PMap ! PMapSendGameMessage(name, GameWhisper("Server", name, msg))
            println("Sending \"" + msg + "\" to " + name + ".")
          } else {
            println("Player " + name + " is not online.")
          }
        }
        case "spawn" => { // spawn id player, spawn an NPC with id in player's room.

        }
        case "restart" => {
          Main.restart
        }
        case _ => {
          println("Invalid cheat request!")
        }
      }
    }
    case _ => {
      // Do nothing
    }
  }
}
