package mq.restful.model;

import java.util.Comparator;

public class TreeModelComparator implements Comparator<TreeModel> {

	@Override
	public int compare(TreeModel o1, TreeModel o2) {

		if (o1.getData().getIsLeaf() && !o2.getData().getIsLeaf()) {
			return 1;
		}
		if (!o1.getData().getIsLeaf() && o2.getData().getIsLeaf()) {
			return -1;
		}

		return o1.getLabel().compareTo(o2.getLabel());
	}

}
