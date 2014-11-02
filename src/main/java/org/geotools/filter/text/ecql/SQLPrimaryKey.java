package org.geotools.filter.text.ecql;

public class SQLPrimaryKey {

	private String name;
	private String type;
	private String tableName;

	public SQLPrimaryKey() {

	}

	public SQLPrimaryKey(String name, String type) {

		this.name = name;
		this.type = type;
	}

	public SQLPrimaryKey(String name, String type, String tableName) {

		this.name = name;
		this.type = type;
		this.tableName = tableName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

}
