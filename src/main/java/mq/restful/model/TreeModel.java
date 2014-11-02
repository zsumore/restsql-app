package mq.restful.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "label", "data", "children" })
public class TreeModel {

	@JsonProperty("children")
	private List<TreeModel> children;

	@JsonProperty("data")
	private TreeData data;

	@JsonIgnore
	private String parent;

	@JsonProperty("label")
	private String label;

	public TreeModel() {

	}

	public TreeModel(List<TreeModel> children, TreeData data) {

		this.children = children;
		this.data = data;
	}

	public List<TreeModel> getChildren() {
		return children;
	}

	public void setChildren(List<TreeModel> children) {
		this.children = children;
	}

	public TreeData getData() {
		return data;
	}

	public void setData(TreeData data) {
		this.data = data;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "TreeModel [data=" + data + ", label=" + label + "]";
	}

}
