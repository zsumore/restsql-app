package mq.restful.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TreeRoot {

	@JsonProperty("treedata")
	private List<TreeModel> treedata;

	public TreeRoot() {

	}

	public TreeRoot(List<TreeModel> treedata) {

		this.treedata = treedata;
	}

	public List<TreeModel> getTreedata() {
		return treedata;
	}

	public void setTreedata(List<TreeModel> treedata) {
		this.treedata = treedata;
	}

}
