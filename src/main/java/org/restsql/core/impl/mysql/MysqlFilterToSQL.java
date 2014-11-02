package org.restsql.core.impl.mysql;

import java.io.IOException;
import java.io.Writer;

import org.geotools.filter.FilterCapabilities;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Function;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.restsql.core.filter.function.FilterFunction_date_format;
import org.restsql.core.impl.BaseFilterToSQL;

@SuppressWarnings("deprecation")
public class MysqlFilterToSQL extends BaseFilterToSQL {

	public MysqlFilterToSQL() {
		super();

	}

	public MysqlFilterToSQL(Writer out) {
		super(out);

	}

	@Override
	protected FilterCapabilities createFilterCapabilities() {
		// TODO Auto-generated method stub
		FilterCapabilities capabilities = new FilterCapabilities();

		capabilities.addAll(FilterCapabilities.LOGICAL_OPENGIS);
		capabilities.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
		capabilities.addType(PropertyIsNull.class);
		capabilities.addType(PropertyIsBetween.class);
		capabilities.addType(Id.class);
		capabilities.addType(IncludeFilter.class);
		capabilities.addType(ExcludeFilter.class);
		capabilities.addType(PropertyIsLike.class);

		// temporal filters
		capabilities.addType(After.class);
		capabilities.addType(Before.class);
		capabilities.addType(Begins.class);
		capabilities.addType(BegunBy.class);
		capabilities.addType(During.class);
		capabilities.addType(Ends.class);
		capabilities.addType(EndedBy.class);
		capabilities.addType(TContains.class);
		capabilities.addType(TEquals.class);

		// function
		capabilities.addType(FilterFunction_date_format.class);

		return capabilities;
	}

	@Override
	public Object visit(Function function, Object extraData)
			throws RuntimeException {
		// TODO Auto-generated method stub
		try {
			encodingFunction = true;
			boolean encoded = visitFunction(function, extraData);
			encodingFunction = false;

			if (encoded) {
				return extraData;
			} else {
				return super.visit(function, extraData);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected boolean visitFunction(Function function, Object extraData)
			throws IOException {

		return false;

	}

	@Override
	protected String getFunctionName(Function function) {

		if (function instanceof FilterFunction_date_format) {
			return "date_format";
		} 
		return function.getName();
	}

}
