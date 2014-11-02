package org.restsql.core;

import org.geotools.filter.text.ecql.MySqlVisitor;

public interface DBDialect {

	public SqlResourceMetaData getSqlResourceMetaData();

	public MySqlVisitor getCustomVisitor();
}
