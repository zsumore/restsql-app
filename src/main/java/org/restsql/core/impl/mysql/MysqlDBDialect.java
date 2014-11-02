package org.restsql.core.impl.mysql;

import org.geotools.filter.text.ecql.MySqlVisitor;
import org.restsql.core.DBDialect;
import org.restsql.core.SqlResourceMetaData;

public class MysqlDBDialect implements DBDialect {

	private SqlResourceMetaData sqlResourceMetaData;

	private MySqlVisitor sqlVisitor;

	public MysqlDBDialect() {

		this.sqlResourceMetaData = new MySqlSqlResourceMetaData();

		this.sqlVisitor = new MysqlFilterToSQL();

	}

	@Override
	public SqlResourceMetaData getSqlResourceMetaData() {

		return this.sqlResourceMetaData;
	}

	@Override
	public MySqlVisitor getCustomVisitor() {

		return this.sqlVisitor;
	}
}
