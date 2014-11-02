package mq.restful.web.rest;

import java.util.List;

import mq.restful.model.TreeModel;

public interface ResTreeService {

	public List<TreeModel> getResourceTree(String path);

}
