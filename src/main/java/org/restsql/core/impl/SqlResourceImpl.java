package org.restsql.core.impl;

import java.math.BigDecimal;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mq.restful.util.RestUtil;

import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.SqlECQL;
import org.opengis.filter.Filter;
import org.restsql.core.ColumnMetaData;
import org.restsql.core.Config;
import org.restsql.core.DBDialect;
import org.restsql.core.RequestSQLParams;
import org.restsql.core.SqlResource;
import org.restsql.core.SqlResourceFactory;
import org.restsql.core.SqlResourceMetaData;
import org.restsql.core.SqlStruct;
import org.restsql.core.TableMetaData.TableRole;
import org.restsql.core.impl.mysql.MysqlDBDialect;
import org.restsql.core.impl.postgresql.PostgresqlDBDialect;
import org.restsql.core.sqlresource.SqlResourceDefinition;
import org.restsql.core.sqlresource.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SqlResourceImpl implements SqlResource {

	private final SqlResourceDefinition definition;
	private final SqlResourceMetaData metaData;
	private final String name;
	private final ObjectMapper objectMapper;

	private SqlResourceFactory sqlResourceFactory;

	Logger logger;

	DBDialect dbDialect;

	public SqlResourceImpl(final String name,
			final SqlResourceDefinition definition,
			final SqlResourceMetaData metaData, SqlResourceFactory factory) {
		this.name = name;
		this.definition = definition;
		definition.getQuery().setValue(definition.getQuery().getValue());
		this.metaData = metaData;
		this.sqlResourceFactory = factory;

		logger = LoggerFactory.getLogger("[" + name + "]");

		objectMapper = new ObjectMapper();

		// dbDialect=getDBDialectByName();
		dbDialect = getDBDialectByName(this.sqlResourceFactory.getConfig()
				.getProperty(Config.KEY_DATABASE_TYPE));

		int size = this.definition.getMetadata().getTable().size();
		for (int i = 0; i < size; i++) {
			Table table = this.definition.getMetadata().getTable().get(i);
			if (table.getRole().equalsIgnoreCase("Parent")) {
				dbDialect.getCustomVisitor().getSQLPrimaryKey()
						.setTableName(table.getName());
				if (table.getPkey() != null) {
					dbDialect.getCustomVisitor().getSQLPrimaryKey()
							.setName(table.getPkey());
				}
				if (table.getPkeyType() != null) {
					dbDialect.getCustomVisitor().getSQLPrimaryKey()
							.setType(table.getPkeyType());
				}
				break;
			}
		}

	}

	@Override
	public SqlResourceDefinition getDefinition() {
		return this.definition;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public SqlResourceMetaData getMetaData() {
		return this.metaData;
	}

	@Override
	public String buildSQL(RequestSQLParams params) throws CQLException,
			FilterToSQLException {

		String filterClause = null;
		if (RestUtil.stringNotNullOrEmpty(params.getFilter())) {
			Filter filter = SqlECQL.toFilter(params.getFilter(), SqlUtils.ff);

			filterClause = this.dbDialect.getCustomVisitor().encodeToString(
					filter);
		}

		SqlStruct struct = new SqlStruct(this.definition.getQuery().getValue(),
				filterClause);

		if (null != params.getLimit())
			struct.setLimit(params.getLimit());

		if (null != params.getOffset())
			struct.setOffset(params.getOffset());

		struct.setOrderByClause(SqlUtils.buildSQLOrderByClause(params
				.getOrderby()));

		struct.setGroupByClause(params.getGroupby());

		String sql = struct.getStructSql();

		logger.info(sql);
		return sql;
	}

	@Override
	public Object read(RequestSQLParams params) throws CQLException,
			DataAccessException, FilterToSQLException {

		String sql = buildSQL(params);

		SqlRowSet resultSet = this.metaData.getJdbcOperations().queryForRowSet(
				sql);

		if (metaData.isHierarchical()) {
			return buildReadResultsHierachicalCollection(resultSet);
		} else {

			return buildReadResultsFlatCollection(resultSet, params);

		}

	}

	private List<JsonNode> buildReadResultsFlatCollection(
			final SqlRowSet resultSet, final RequestSQLParams params) {
		List<JsonNode> listNode = new ArrayList<JsonNode>();
		while (resultSet.next()) {

			ObjectNode objectNode = objectMapper.createObjectNode();

			for (final ColumnMetaData columnData : metaData.getAllReadColumns()) {
				if (!columnData.isNonqueriedForeignKey()) {
					boolean addNode = true;
					if (params.getVisible() != null
							&& params.getVisible().length() > 0) {
						if (columnData.getColumnNumber() <= params.getVisible()
								.length()
								&& params.getVisible().charAt(
										columnData.getColumnNumber() - 1) == '0') {
							addNode = false;
						}

					}
					if (addNode)
						addJsonNodeRow(objectNode, columnData, resultSet);
				}

			}

			listNode.add(objectNode);
		}

		return listNode;

	}

	private void addJsonNodeRow(ObjectNode objectNode,
			ColumnMetaData columnData, SqlRowSet resultSet) {

		String column = columnData.getColumnLabel();

		Object value = SqlUtils.getObjectByColumnNumber(columnData, resultSet);
		Format formatter = definition.getFormatter(column);
		if (value == null) {
			if (null != definition.getNullValue(column)) {
				if (definition.getAttributeType(column).equalsIgnoreCase(
						"Numeric")) {
					if (definition.getNullValue(column).contains(".")) {
						objectNode
								.put(column, Double.valueOf(definition
										.getNullValue(column)));
					} else {
						objectNode.put(column,
								Long.valueOf(definition.getNullValue(column)));
					}
				} else if (definition.getAttributeType(column)
						.equalsIgnoreCase("String")) {
					objectNode.put(column, definition.getNullValue(column));
				} else {
					objectNode.putNull(column);
				}
			} else {
				objectNode.putNull(column);
			}

		} else if (value instanceof Integer) {
			if (null != formatter) {
				objectNode.put(column,
						Integer.valueOf(formatter.format((Integer) value)));
			} else {
				objectNode.put(column, (Integer) value);
			}
		} else if (value instanceof String) {
			if (null != definition.getStringReplacementList(column)
					&& definition.getStringReplacementList(column).size() > 0) {
				int size = definition.getStringReplacementList(column).size();

				for (int i = 0; i < size; i++) {

					value = ((String) value).replaceAll(
							definition.getStringReplacementList(column).get(i)
									.getRegex(), definition
									.getStringReplacementList(column).get(i)
									.getReplacement());

				}

			}

			objectNode.put(column, (String) value);

		} else if (value instanceof Boolean) {
			objectNode.put(column, (Boolean) value);
		} else if (value instanceof Date) {
			if (null != formatter) {
				objectNode.put(column, formatter.format((Date) value));
			} else {
				objectNode.put(column, ((Date) value).getTime());
			}
		} else if (value instanceof Long) {
			if (null != formatter) {
				objectNode.put(column,
						Long.valueOf(formatter.format((Long) value)));
			} else {
				objectNode.put(column, (Long) value);
			}
		} else if (value instanceof Double) {
			if (null != formatter) {
				objectNode.put(column,
						Double.valueOf(formatter.format((Double) value)));
			} else {
				objectNode.put(column, (Double) value);
			}
		} else if (value instanceof Float) {
			if (null != formatter) {
				objectNode.put(column,
						Float.valueOf(formatter.format((Float) value)));
			} else {
				objectNode.put(column, (Float) value);
			}
		} else if (value instanceof BigDecimal) {
			if (null != formatter) {
				objectNode.put(column,
						new BigDecimal(formatter.format((BigDecimal) value)));
			} else {
				objectNode.put(column, (BigDecimal) value);
			}
		} else if (value instanceof Byte) {
			objectNode.put(column, (Byte) value);
		} else if (value instanceof byte[]) {
			objectNode.put(column, (byte[]) value);
		} else {
			logger.error("Unmappable object type: {}", value.getClass());
			throw new IllegalArgumentException("Unmappable object type: "
					+ value.getClass());
		}
	}

	private List<Map<String, Object>> buildReadResultsHierachicalCollection(
			SqlRowSet resultSet) {
		final List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		final List<Object> currentParentPkValues = new ArrayList<Object>(
				metaData.getParent().getPrimaryKeys().size());

		boolean newParent = false;
		final int numberParentElementColumns = metaData.getParentReadColumns()
				.size();
		// final int numberChildElementColumns =
		// metaData.getChildReadColumns().size();
		final String childRowsName = getChildRowsName();
		Map<String, Object> parentRow = null;
		List<JsonNode> childRows = null;

		while (resultSet.next()) {
			// Assess state of parent
			if (currentParentPkValues.isEmpty()) {
				// First row
				newParent = true;
			} else {
				// Not the first row, check if parent differs from the last
				newParent = false;
				for (int i = 0; i < currentParentPkValues.size(); i++) {
					final ColumnMetaData column = metaData.getParent()
							.getPrimaryKeys().get(i);
					if (!currentParentPkValues.get(i).equals(
							SqlUtils.getObjectByColumnLabel(column, resultSet))) {
						newParent = true;
						break;
					}
				}
			}

			// Set current parent row pk values as well as in the parent row
			// object
			if (newParent) {
				childRows = new ArrayList<JsonNode>();
				parentRow = new HashMap<String, Object>(
						numberParentElementColumns);
				parentRow.put(childRowsName, childRows);
				results.add(parentRow);
				currentParentPkValues.clear();

				for (final ColumnMetaData column : metaData
						.getParentReadColumns()) {
					final Object value = SqlUtils.getObjectByColumnLabel(
							column, resultSet);
					if (column.isPrimaryKey()
							&& column.getTableRole() == TableRole.Parent) {
						currentParentPkValues.add(value);
					}
					parentRow.put(column.getColumnLabel(), value);
				}

			}

			// Populate the child row object
			ObjectNode childRow = objectMapper.createObjectNode();
			boolean nullPk = false;
			for (final ColumnMetaData column : metaData.getChildReadColumns()) {
				final Object value = SqlUtils.getObjectByColumnLabel(column,
						resultSet);
				if (column.isPrimaryKey()) {
					nullPk = value == null;
				}
				if (null == value) {
					childRow.putNull(column.getColumnLabel());
				} else {
					addJsonNodeRow(childRow, column, resultSet);
				}
			}

			if (nullPk) {
				childRow = null;
			} else {
				childRows.add(childRow);
			}

		}

		return results;

	}

	private String getChildRowsName() {
		return new StringBuilder(metaData.getChild().getTableAlias()).append(
				"s").toString();
	}

	@Override
	public SqlResourceFactory getSqlResourceFactory() {

		return this.sqlResourceFactory;
	}

	private DBDialect getDBDialectByName(String name) {

		if (name.equalsIgnoreCase(Config.POSTGRESQL_DB_DIALECT)) {

			return new PostgresqlDBDialect();

		} else if (name.equalsIgnoreCase(Config.MYSQL_DB_DIALECT)) {

			return new MysqlDBDialect();

		}
		return null;
	}

}
