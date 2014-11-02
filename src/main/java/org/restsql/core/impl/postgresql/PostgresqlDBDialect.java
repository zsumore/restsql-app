package org.restsql.core.impl.postgresql;

import org.geotools.filter.text.ecql.MySqlVisitor;
import org.restsql.core.DBDialect;
import org.restsql.core.SqlResourceMetaData;

public class PostgresqlDBDialect implements DBDialect {

	private SqlResourceMetaData sqlResourceMetaData;

	private MySqlVisitor sqlVisitor;

	public PostgresqlDBDialect() {

		this.sqlResourceMetaData = new PostgreSqlSqlResourceMetaData();

		this.sqlVisitor = new PostgresqlFilterToSQL();

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
