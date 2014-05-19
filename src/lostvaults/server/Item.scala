import scala.io.Source

package lostvaults.server
/**
 * The class item is a constructor for items in the game. An item is  
 * composed of the name of the item, and the value of it.
 */

class Item (_name : String, _value : Int) {
	val name = _name
	var value = _value
	
	
}
	def readItemsFromFile() {
      println("Following is the content read:" )

      Source.fromFile("test.txt" ).foreach { 
         print 
      }
	}
      def createItems() {
        
      }
      
      def updateItems() {   
      }


