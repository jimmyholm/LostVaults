package lostvaults.server

class Item (_name : String, _value : Int) {
	val name : String = _name
	var value = _value
  	
	  
	// getters and setters
	def getName = {
	  name
	} 	
	def getValue = {
	  value
	}
	def setValue (_value : Int) {
	  value = _value
	}
}