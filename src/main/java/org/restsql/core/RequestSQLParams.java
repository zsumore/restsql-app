package org.restsql.core;

public class RequestSQLParams {

	private String filter, orderby, resName, groupby, visible;
	private Integer limit = -1, offset = -1;

	// public RequestSQLParams(String resName, String filter, String orderby,
	// String groupby, Integer limit, Integer offset) {
	// this.resName = resName;
	// this.filter = filter.trim();
	// this.orderby = orderby.trim();
	// this.groupby = groupby.trim();
	// if (null != limit)
	// this.limit = limit;
	// if (null != offset)
	// this.offset = offset;
	// }

	public RequestSQLParams() {

	}

	public String getFilter() {
		return filter;
	}

	public String getOrderby() {
		return orderby;
	}

	public Integer getLimit() {
		return limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public String getResName() {
		return resName;
	}

	public void setResName(String resName) {
		this.resName = resName;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public String getGroupby() {
		return groupby;
	}

	public void setGroupby(String groupby) {
		this.groupby = groupby;
	}

	public String getVisible() {
		return visible;
	}

	public void setVisible(String vis) {
		if (vis != null)
			this.visible = vis.trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + ((groupby == null) ? 0 : groupby.hashCode());
		result = prime * result + ((limit == null) ? 0 : limit.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result + ((orderby == null) ? 0 : orderby.hashCode());
		result = prime * result + ((resName == null) ? 0 : resName.hashCode());
		result = prime * result + ((visible == null) ? 0 : visible.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestSQLParams other = (RequestSQLParams) obj;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		if (groupby == null) {
			if (other.groupby != null)
				return false;
		} else if (!groupby.equals(other.groupby))
			return false;
		if (limit == null) {
			if (other.limit != null)
				return false;
		} else if (!limit.equals(other.limit))
			return false;
		if (offset == null) {
			if (other.offset != null)
				return false;
		} else if (!offset.equals(other.offset))
			return false;
		if (orderby == null) {
			if (other.orderby != null)
				return false;
		} else if (!orderby.equals(other.orderby))
			return false;
		if (resName == null) {
			if (other.resName != null)
				return false;
		} else if (!resName.equals(other.resName))
			return false;
		if (visible == null) {
			if (other.visible != null)
				return false;
		} else if (!visible.equals(other.visible))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RequestSQLParams [filter=" + filter + ", orderby=" + orderby
				+ ", resName=" + resName + ", groupby=" + groupby
				+ ", visible=" + visible + ", limit=" + limit + ", offset="
				+ offset + "]";
	}

}
