package cql.test;

import java.io.File;
import java.net.URISyntaxException;

import mq.restful.util.MyFilterToSQL;

import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.filter.Filter;

public class Temp {

	public static void main(String[] args) throws CQLException,
			FilterToSQLException {
		
		String s="11.11";
		
		System.out.println(s.contains("."));
		// TODO Auto-generated method stub
		/*String main = "dd abc GROUP BY a b ";
		int index = main.indexOf("GROUP BY");

		String s1 = main.substring(0, index);
		String s2 = main.substring(index);

		System.out.println(index);
		System.out.println(s1);
		System.out.println(s2);

		String filterStr = "datetime DURING 2014-03-21T14:00:00Z/2014-03-21T14:00:01Z";

		// String filterStr = "datetime=2014-03-21T14:00:00Z";

		// String filterClause = filterStr.replaceAll("\\+", " ");

		// System.out.println(filterClause);
		Filter filter = ECQL.toFilter(filterStr);

		
		 * System.out.println(filter.getClass());
		 * System.out.println(filter.toString());
		 * System.out.println(filter.getExpression1().getClass());
		 * 
		 * System.out.println(filter.getExpression1());
		 * 
		 * System.out.println(filter.getExpression2().getClass());
		 * 
		 * System.out.println(filter.getExpression2());
		 * 
		 * LiteralExpressionImpl l=(LiteralExpressionImpl)
		 * filter.getExpression2();
		 * 
		 * System.out.println(l.getType());
		 * System.out.println(l.getValue().toString());
		 

		MyFilterToSQL vistor = new MyFilterToSQL();
		String sql = vistor.encodeToString(filter);*/

		//System.out.println(sql);

	}

}
