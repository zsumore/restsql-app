package jaxb;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFoo {

	Foo foo;

	@Before
	public void setUp() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");

		foo = new Foo();
		foo.setValues(list);
	}

	@After
	public void tearDown() throws Exception {
		foo = null;
	}

	@Test
	public void test() {

		try {

			File file = new File("D:\\foo.xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(Foo.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(foo, file);
			jaxbMarshaller.marshal(foo, System.out);

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		fail("Not yet implemented");
	}

}
