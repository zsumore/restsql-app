package mq.restful.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "rid", "description", "isLeaf" })
public class TreeData {

	@JsonProperty("description")
	private String description;
	@JsonProperty("rid")
	private String rid;
	@JsonProperty("isLeaf")
	private Boolean isLeaf;

	public TreeData(String description, String rid, Boolean isLeaf) {

		this.description = description;
		this.rid = rid;
		this.isLeaf = isLeaf;
	}

	public TreeData() {

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public Boolean getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(Boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

}
