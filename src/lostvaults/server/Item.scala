package lostvaults.server
import scala.io.Source

/**
 * The class item is a constructor for items in the game. An item is
 * composed of the name of the item, and the value of it.
 */

class Item(_name: String, _value: Int) {
  val name = _name
  var value = _value

  /*
 * Ska läsa in alla textrader från en fil
 * 
 */
  def readItemsFromFile() {
    println("Following is the content read:")

    //      Source.fromFile("test.txt" ).foreach { 
    //         print 
    //      }

    for (line <- Source.fromFile("test.txt").getLines())
      println(line)
  }
  /*
   *   Denna funktion ska skapa items från det datat som läses in från den givna filen.
   */

  def createItems() {
    val name = ""
    var attack = 0
    var armor = 0
    var itemBase: Array[String] = Array()
    val it = readItemsFromFile()

        for (line <- Source.fromFile("test.txt").getLines()){
      //println(line)
      
      if (line == "[ITEM]") {
      	
      if (line == "Name: ") {
       
      	  var name = readChar()
    		
      	}
      else if (line == "Attack: ") {
    	acc += 1
        	var attack = readInt()
    		
    	}
      else if line == "Armor: "){
    	acc += 1 
    	  var armor = readInt()
    		
    	}
      else if line == "Speed: "){
    	acc += 1 
    	  var armor = readInt()
    		
    	}
      else if (line == "[/ITEM]" ){
    	  
    	}
      }
      else {
         println ("The format of the file is wrong")
      }    
   }
}
    // skriv en loop som tar varje rad, tills "\n" kommer, och gör ett item av givna rader:


  /*
       * Denna funktion ska uppdatera platsen där alla items är lagrade med den nyaste versionen.
       * 
       */
  def updateItems() {
  }

}



