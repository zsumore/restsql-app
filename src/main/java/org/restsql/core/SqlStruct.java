package org.restsql.core;

import org.restsql.core.impl.SqlUtils;

public class SqlStruct {

	private StringBuffer clause;
	private int limit = -1;
	private StringBuffer main;
	private int offset = -1;
	private StringBuffer orderByClause;
	private StringBuffer groupByClause;

	public SqlStruct(String mainStr, String clauseStr) {
		main = new StringBuffer();
		main.append(mainStr.trim());

		clause = new StringBuffer();
		if (clauseStr != null)
			clause.append(clauseStr.trim());

	}

	public StringBuffer getOrderByClause() {
		return orderByClause;
	}

	public void setOrderByClause(String orderByStr) {
		this.orderByClause = new StringBuffer();
		if (null != orderByStr && orderByStr.trim().length() > 0)
			this.orderByClause.append(" ORDER BY ").append(orderByStr);

	}

	public StringBuffer getGroupByClause() {
		return groupByClause;
	}

	public void setGroupByClause(String groupByStr) {
		this.groupByClause = new StringBuffer();
		if (null != groupByStr && groupByStr.trim().length() > 0)
			this.groupByClause.append(" GROUP BY ").append(groupByStr);
	}

	public StringBuffer getClause() {
		return clause;
	}

	public int getLimit() {
		return limit;
	}

	public StringBuffer getMain() {
		return main;
	}

	public int getOffset() {
		return offset;
	}

	public boolean isClauseEmpty() {
		return clause.length() == 0;
	}

	public void setLimit(final int limit) {
		this.limit = limit;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public String getStructSql() {

		StringBuffer struct = new StringBuffer();

		String tmp = SqlUtils.removeWhitespaceFromSql(main.toString());
		String tmpGroupBy = null;
		String tmpMain = null;
		if (tmp.indexOf("GROUP") > 0 || tmp.indexOf("group") > 0) {
			int max = tmp.indexOf("GROUP");
			int min = tmp.indexOf("group");
			int index = max > min ? max : min;
			tmpMain = tmp.substring(0, index);
			tmpGroupBy = tmp.substring(index);
		} else {
			tmpMain = tmp;
		}

		struct.append(tmpMain);
		if (this.clause.length() > 0)

		{
			if (struct.indexOf("where ") > 0 || struct.indexOf("WHERE ") > 0) {
				String clauseString = this.clause.toString()
						.replaceFirst("where ", "").replaceFirst("WHERE ", "");
				struct.append(" AND ").append(clauseString);
			} else if (this.clause.indexOf("where ") >= 0
					|| this.clause.indexOf("WHERE ") >= 0) {
				struct.append(" ").append(this.clause.toString().trim());
			} else {
				struct.append(" WHERE ").append(this.clause.toString().trim());
			}
		}
		if (null != this.groupByClause && this.groupByClause.length() > 0) {
			struct.append(" ").append(this.groupByClause.toString());
		} else if (null != tmpGroupBy) {
			struct.append(" ").append(tmpGroupBy);
		}
		if (null != this.orderByClause && this.orderByClause.length() > 0) {
			struct.append(" ").append(this.orderByClause.toString());
		}
		if (this.limit > 0) {

			struct.append(" LIMIT ");
			struct.append(this.limit);

		}
		if (this.offset >= 0) {
			struct.append(" OFFSET ");
			struct.append(this.offset);
		}

		return struct.toString().trim();
	}
}
