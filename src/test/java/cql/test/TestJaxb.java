package cql.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.restsql.core.sqlresource.ObjectFactory;
import org.restsql.core.sqlresource.SqlResourceDefinition;

public class TestJaxb {
	public static void main(String[] args) throws JAXBException, IOException  {

		InputStream inputStream =new FileInputStream("D:/nh_sum_r8_with_csv_header.xml");
		JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			unmarshaller.setSchema(null);
			final SqlResourceDefinition definition = ((JAXBElement<SqlResourceDefinition>) unmarshaller
					.unmarshal(inputStream)).getValue();
			
			
			if(null!=definition){
				String s=definition.getRestConfig().getCsvConfig().getLineSeparator();
				System.out.println(s.length());
				String b=s.replaceAll("\\\\n", "\n");
				System.out.println(b);
				//System.out.println("\n");
			}
			
			if (inputStream != null) {
				
					inputStream.close();
				
			}

}
}
