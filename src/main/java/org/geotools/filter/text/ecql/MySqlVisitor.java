package org.geotools.filter.text.ecql;

import org.restsql.core.SqlVisitor;

public interface MySqlVisitor extends SqlVisitor {
	
	public void setSQLPrimaryKey(SQLPrimaryKey key);
	
	public SQLPrimaryKey getSQLPrimaryKey();
	

}
