package org.restsql.core.sqlresource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CsvConfig")
public class CsvConfig {

	@XmlAttribute(name = "useHeader")
	protected Boolean useHeader;

	@XmlAttribute(name = "lineSeparator")
	protected String lineSeparator;

	@XmlAttribute(name = "columnSeparator")
	protected String columnSeparator;

	public Boolean getUseHeader() {
		return useHeader;
	}

	public void setUseHeader(Boolean useHeader) {
		this.useHeader = useHeader;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	public String getColumnSeparator() {
		return columnSeparator;
	}

	public void setColumnSeparator(String columnSeparator) {
		this.columnSeparator = columnSeparator;
	}

	@Override
	public String toString() {
		return "CsvConfig [useHeader=" + useHeader + ", lineSeparator="
				+ lineSeparator + ", columnSeparator=" + columnSeparator + "]";
	}

}
