package cql.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestChar {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String test="南海001111";
		System.out.println(test.length());
		System.out.println(test.charAt(0));
		if(test.charAt(0)=='0'){
			System.out.println("hello");
		}
		fail("Not yet implemented");
	}

}
