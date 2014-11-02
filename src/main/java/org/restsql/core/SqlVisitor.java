package org.restsql.core;

import org.geotools.data.jdbc.FilterToSQLException;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;

public interface SqlVisitor extends FilterVisitor, ExpressionVisitor{
	
	public boolean fullySupports(org.opengis.filter.Filter filter);
	
	public String encodeToString(Filter filter) throws FilterToSQLException;
	
	public String encodeToString(Expression expression) throws FilterToSQLException;

}
