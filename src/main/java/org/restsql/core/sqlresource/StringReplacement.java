package org.restsql.core.sqlresource;

public class StringReplacement {

	private String regex = "";

	private String replacement = "";

	public StringReplacement(String regex, String replacement) {

		this.regex = regex;
		if (null != replacement)
			this.replacement = replacement;
	}

	public StringReplacement() {

	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

}
