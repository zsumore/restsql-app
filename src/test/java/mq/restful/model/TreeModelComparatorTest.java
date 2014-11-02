package mq.restful.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TreeModelComparatorTest {
	
	TreeModel o1; TreeModel o2;

	@Before
	public void setUp() throws Exception {
		o1=new TreeModel();
		o1.setLabel("a");
		
		TreeData data1=new TreeData(); 
		data1.setIsLeaf(false);
		
		
		o2=new TreeModel();
		o2.setLabel("b");
		
	}

	@After
	public void tearDown() throws Exception {
		o1=null;
		o2=null;
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
