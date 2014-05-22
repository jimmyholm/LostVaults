package lostvaults.util
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.JdbcBackend
import scala.slick.driver.SQLiteDriver.simple._
import scala.Console
import Q.interpolation
import java.io._
object ItemAdder {
  import JdbcBackend.Database
  def main(args: Array[String]) {
    Database.forURL("jdbc:sqlite:lostvaults.db", driver = "org.sqlite.JDBC") withSession {
      implicit session =>
        var input = ""
        var name = ""
        var minAttack = 0
        var maxAttack = 0
        var minDefense = 0
        var maxDefense = 0
        var minSpeed = 0
        var maxSpeed = 0
        var rating = 0
        var itemCount = 0
        var treasureIncrease = 0
        val ItemDBFile = new FileOutputStream("database.txt", true)
        val Printer = new PrintWriter(ItemDBFile)
        do {
          itemCount = 0
          input = Console.readLine("Add item type (Weapon, Armor, Treasure) or Quit to exit > ")
          input.toUpperCase match {
            case "WEAPON" => {
              name = Console.readLine("Enter the name of the new weapon > ")
              minAttack = Console.readLine("Enter the minimum attack value of the new weapon > ").toInt
              maxAttack = Console.readLine("Enter the maximum attack value of the new weapon > ").toInt
              minSpeed = Console.readLine("Enter the minimum speed value of the new weapon > ").toInt
              maxSpeed = Console.readLine("Enter the maximum speed value of the new weapon > ").toInt
              minDefense = Console.readLine("Enter any defense bonus of the new weapon > ").toInt
              maxDefense = minDefense
              rating = Console.readLine("Enter the rating of the new weapon > ").toInt
              for (a <- minAttack to maxAttack) {
                for (s <- minSpeed to maxSpeed) {
                  var sql = "INSERT INTO Items (name, attack, defense, speed, rating, itemType) VALUES " +
                    "('" + name + " (" + a + ")', " + a + ", " + minDefense + ", " + s + ", " + rating + ", 'Weapon');"
                  Printer.write(sql + "\n"); Printer.flush()
                  (Q.u + sql).execute
                  itemCount += 1
                }
              }
            }
            case "ARMOR" => {
              name = Console.readLine("Enter the name of the new armor > ")
              minDefense = Console.readLine("Enter the minimum defense value of the new armor > ").toInt
              maxDefense = Console.readLine("Enter the maximum defense value of the new armor > ").toInt
              minSpeed = Console.readLine("Enter the minimum speed value of the new armor > ").toInt
              maxSpeed = Console.readLine("Enter the maximum speed value of the new armor > ").toInt
              minAttack = Console.readLine("Enter any attack bonus of the new armor > ").toInt
              maxAttack = minAttack
              rating = Console.readLine("Enter the rating of the new armor > ").toInt
              for (d <- minDefense to maxDefense) {
                for (s <- minSpeed to maxSpeed) {
                  var sql = "INSERT INTO Items (name, attack, defense, speed, rating, itemType) VALUES " +
                    "('" + name + " ("+d+")', " + minAttack + ", " + d + ", " + s + ", " + rating + ", 'Armor');"
                  Printer.write(sql + "\n"); Printer.flush()
                  (Q.u + sql).execute
                  itemCount += 1
                }
              }
            }
            case "TREASURE" => {
              name = Console.readLine("Enter the name of the new treasure > ")
              minAttack = Console.readLine("Enter the minimum gold value of the new treasure > ").toInt
              maxAttack = Console.readLine("Enter the maximum gold value of the new treasure > ").toInt
              treasureIncrease = Console.readLine("Enter the steps to increase value by > ").toInt
              treasureIncrease = if(treasureIncrease < 1) 1 else treasureIncrease
              rating = Console.readLine("Enter the rating of the new treasure > ").toInt
              for (v <- minAttack to maxAttack by treasureIncrease) {
                var sql = "INSERT INTO Items (name, attack, defense, speed, rating, itemType) VALUES " +
                  "('" + name + " (" + v + ")', " + v + ", 0, 0," + rating + ", 'Treasure');"
                Printer.write(sql + "\n"); Printer.flush()
                (Q.u + sql).execute
                itemCount += 1
              }
            }
            case s => {
              if (s.compareToIgnoreCase("Quit") != 0)
                println("Not a valid item type!")
            }
          }
          if(input.compareToIgnoreCase("Quit") != 0)
            println("Wrote " + itemCount + " item variations to the database.")
        } while (input.compareToIgnoreCase("Quit") != 0)
        Printer.close()
        ItemDBFile.close()
    }
  }
}