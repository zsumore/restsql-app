/* Copyright (c) restSQL Project Contributors. Licensed under MIT. */
package org.restsql.core.impl;

import java.sql.SQLException;
import java.sql.Types;

import mq.restful.util.RestUtil;

import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.restsql.core.ColumnMetaData;
import org.restsql.core.Config;
import org.restsql.core.DBDialect;
import org.restsql.core.impl.mysql.MysqlDBDialect;
import org.restsql.core.impl.postgresql.PostgresqlDBDialect;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

/**
 * Contains utilities to manage SQL and java.sql.ResultSets.
 * 
 * @author Mark Sawers
 */
public class SqlUtils {

	// private static final MyFilterToSQL filterToSQL = new MyFilterToSQL();

	public static final FilterFactory ff = CommonFactoryFinder
			.getFilterFactory();

	// private static DBDialect postgresql, mysql;

	public static Object getObjectByColumnLabel(final ColumnMetaData column,
			final SqlRowSet resultSet) {
		Object value = null;
		if (column.getColumnType() == Types.DATE
				&& column.getColumnTypeName().equals("YEAR")) {
			value = new Integer(resultSet.getInt(column.getColumnLabel()));
			if (resultSet.wasNull()) {
				value = null;
			}
		} else {
			value = resultSet.getObject(column.getColumnLabel());
		}

		return value;
	}

	public static Object getObjectByColumnNumber(final ColumnMetaData column,
			final SqlRowSet resultSet) {
		Object value = null;
		if (column.getColumnType() == Types.DATE
				&& column.getColumnTypeName().equals("YEAR")) {
			value = new Integer(resultSet.getInt(column.getColumnNumber()));
			if (resultSet.wasNull()) {
				value = null;
			}
		} else {
			value = resultSet.getObject(column.getColumnNumber());
		}
		return value;
	}

	public static String removeWhitespaceFromSql(String sql) {
		sql.replaceAll("\\n", " ");
		sql = sql.replaceAll("\\r", " ");
		sql = sql.replaceFirst("\\s+", " ");
		sql = sql.replaceFirst("\\t+", " ");
		sql = sql.replaceFirst("\\t+$", " ");
		sql = sql.replaceAll("\\t", " ");
		return sql;
	}

	public static String buildSQLFilterClause(String filterStr, String dbtype)
			throws CQLException, FilterToSQLException {

		String result = null;
		if (RestUtil.stringNotNullOrEmpty(filterStr)) {
			// System.out.println("----------");
			// String filterClause = filterStr.replaceAll("\\+", " ");
			// System.out.println("-----2-----");
			// filterClause = filterClause.replaceAll("%20", " ");
			// System.out.println(filterClause);
			Filter filter = ECQL.toFilter(filterStr, ff);

			// System.out.println(filter.toString());

			result = getDBDialectByName(dbtype).getCustomVisitor()
					.encodeToString(filter);

		}
		return result;
	}

	public static DBDialect getDBDialectByName(String name) {

		if (name.equalsIgnoreCase(Config.POSTGRESQL_DB_DIALECT)) {

			return new PostgresqlDBDialect();

		} else if (name.equalsIgnoreCase(Config.MYSQL_DB_DIALECT)) {

			return new MysqlDBDialect();

		}
		return null;
	}

	public static String buildSQLOrderByClause(String orderByStr) {
		if (RestUtil.stringNotNullOrEmpty(orderByStr)) {
			return orderByStr;
			/*
			 * StringBuffer buffer = new StringBuffer(""); if
			 * (orderByStr.indexOf(";") > 0) { String[] list =
			 * orderByStr.trim().split(";"); int size = list.length; for (int i
			 * = 0; i < size; i++) { if (i != 0) { buffer.append(","); }
			 * buffer.append(list[i].replaceAll(":", " ")); } } else {
			 * buffer.append(orderByStr.replaceAll(":", " ")); }
			 * 
			 * return buffer.toString();
			 */

			// if (orderByStr.indexOf(":") != -1) {
			// StringBuffer buffer = new StringBuffer("");
			// String[] list = orderByStr.trim().split(":");
			//
			// int size = list.length;
			//
			// for (int i = 0; i < size - 1; i++) {
			// String tmp = list[i].trim();
			// if (null != tmp && !tmp.equals("")) {
			// if (i != 0) {
			// buffer.append(",");
			// }
			// buffer.append(tmp);
			//
			// }
			// }
			// String last = list[size - 1];
			// if (null != last) {
			// last = last.trim();
			// if (last.length() > 0) {
			// if (last.equalsIgnoreCase("DESC")
			// || last.equalsIgnoreCase("ASC")) {
			//
			// buffer.append(" ");
			//
			// } else {
			// buffer.append(",");
			// }
			// buffer.append(last);
			// }
			// }
			//
			// return buffer.toString();
			// } else {
			// return orderByStr.trim();
			// }
		}

		return "";
	}

	public static String lookupColumnName(SqlRowSetMetaData resultSetMetaData,
			int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name == null || name.length() < 1) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}
}
