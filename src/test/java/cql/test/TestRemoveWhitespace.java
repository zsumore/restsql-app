package cql.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restsql.core.impl.SqlUtils;

public class TestRemoveWhitespace {
	
	String test;

	@Before
	public void setUp() throws Exception {
		test ="select a  from b where c='d    d' group     by";
	}

	@After
	public void tearDown() throws Exception {
		test =null;
	}

	@Test
	public void test() {
		
		System.out.println(SqlUtils.removeWhitespaceFromSql(test));
		
		System.out.println(SqlUtils.removeWhitespaceFromSql(test).replaceFirst("\\s+", " "));
		fail("Not yet implemented");
	}

}
