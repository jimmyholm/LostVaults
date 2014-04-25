package lostvaults;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParserTester {

	@Test
	public void test() {
		String s = Parser.FindWord("This is a test!", 0);
		assertEquals(String.format("\"%s\" should be \"This\"", s), s, "This");
		s = Parser.FindWord("This is a test!", 2);
		assertEquals(String.format("\"%s\" should be \"a\"", s), s, "a");
		s = Parser.FindRest("This is a test!", 1);
		assertEquals(String.format("\"%s\" should be \"a test!\"", s), s, "a test!");
		s = Parser.FindRest("This is a test!", 2);
		assertEquals(String.format("\"%s\" should be \"test!\"", s), s, "test!");
		s = Parser.FindRest("This is a test!", 5);
		assertEquals(String.format("\"%s\" should be \"test!\"", s), s, "test!");
	}

}
