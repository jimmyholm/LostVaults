package lostvaults.tests;
import static org.junit.Assert.*;
import org.junit.Test;
import lostvaults.Parser;

public class ParserTest {

	@Test
	public void test() {
		String s = Parser.findWord("This is a test!", 0);
		assertEquals(String.format("\"%s\" should be \"This\"", s), s, "This");
		s = Parser.findWord("This is a test!", 2);
		assertEquals(String.format("\"%s\" should be \"a\"", s), s, "a");
		s = Parser.findRest("This is a test!", 1);
		assertEquals(String.format("\"%s\" should be \"a test!\"", s), s, "a test!");
		s = Parser.findRest("This is a test!", 2);
		assertEquals(String.format("\"%s\" should be \"test!\"", s), s, "test!");
		s = Parser.findRest("This is a test!", 5);
		assertEquals(String.format("\"%s\" should be \"test!\"", s), s, "test!");
		s = Parser.findRest("This is a test!", 0);
		assertEquals(String.format("\"%s\" should be \"is a test!\"", s), s, "is a test!");
	}

}
