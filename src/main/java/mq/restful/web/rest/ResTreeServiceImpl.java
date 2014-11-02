package mq.restful.web.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mq.restful.model.TreeData;
import mq.restful.model.TreeModel;
import mq.restful.model.TreeModelComparator;

import org.springframework.stereotype.Service;

@Service("resTreeService")
public class ResTreeServiceImpl implements ResTreeService {

	TreeModelComparator c;

	public ResTreeServiceImpl() {

		c = new TreeModelComparator();

	}

	@Override
	public List<TreeModel> getResourceTree(String path) {
		TreeModel treeModel = new TreeModel();
		treeModel.setParent(".");
		treeModel.setChildren(new ArrayList<TreeModel>());
		visitDirTree(path, treeModel);
		return treeModel.getChildren();
	}

	//不访问"."开头的文件夹如：".git"
	private void visitDirTree(final String path, final TreeModel tree) {
		File dir = new File(path);

		File[] dirFileList = dir.listFiles();
		int size = dirFileList.length;
		for (int i = 0; i < size; i++) {
			String fileName = dirFileList[i].getName();
			if (dirFileList[i].isFile() && fileName.endsWith(".xml")) {

				TreeModel leaf = new TreeModel();
				TreeData data = new TreeData();
				// String
				// label=dirFileList[i].getAbsolutePath().replaceFirst(path,
				// "").replaceAll("/", replacement);

				leaf.setLabel(fileName.substring(0, fileName.length() - 4));
				data.setDescription(leaf.getLabel());
				if (tree.getParent().equals(".")) {
					data.setRid(leaf.getLabel());
				} else {
					data.setRid(new StringBuffer(tree.getParent()).append(".")
							.append(leaf.getLabel()).toString());
				}
				data.setIsLeaf(true);

				leaf.setData(data);
				// leaf.setParent(tree.);

				tree.getChildren().add(leaf);

			} else if (dirFileList[i].isDirectory()
					&& !dirFileList[i].getName().startsWith(".")) {
				TreeModel dict = new TreeModel();
				dict.setLabel(dirFileList[i].getName());
				if (tree.getParent().equals(".")) {
					dict.setParent(dict.getLabel());
				} else {
					dict.setParent(new StringBuffer(tree.getParent())
							.append(".").append(dict.getLabel()).toString());
				}
				TreeData data = new TreeData();
				data.setIsLeaf(false);
				data.setDescription(dict.getLabel());
				dict.setData(data);
				dict.setChildren(new ArrayList<TreeModel>());

				tree.getChildren().add(dict);

				visitDirTree(dirFileList[i].getAbsolutePath(), dict);

			}
		}

		Collections.sort(tree.getChildren(), c);

	}

}
