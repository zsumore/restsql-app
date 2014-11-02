package org.restsql.core.impl.postgresql;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

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
import org.restsql.core.filter.function.FilterFunction_date_part;
import org.restsql.core.filter.function.FilterFunction_date_trunc;
import org.restsql.core.filter.function.FilterFunction_to_char;
import org.restsql.core.impl.BaseFilterToSQL;

@SuppressWarnings("deprecation")
public class PostgresqlFilterToSQL extends BaseFilterToSQL {

	public PostgresqlFilterToSQL() {
		super();

	}

	public PostgresqlFilterToSQL(Writer out) {
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
		capabilities.addType(FilterFunction_to_char.class);
		capabilities.addType(FilterFunction_date_part.class);
		capabilities.addType(FilterFunction_date_trunc.class);

		return capabilities;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected String cast(String property, Class target) throws IOException {
		if (String.class.equals(target)) {
			return property + "::text";
		} else if (Short.class.equals(target) || Byte.class.equals(target)) {
			return property + "::smallint";
		} else if (Integer.class.equals(target)) {
			return property + "::integer";
		} else if (Long.class.equals(target)) {
			return property + "::bigint";
		} else if (Float.class.equals(target)) {
			return property + "::real";
		} else if (Double.class.equals(target)) {
			return property + "::float8";
		} else if (BigInteger.class.equals(target)) {
			return property + "::numeric";
		} else if (BigDecimal.class.equals(target)) {
			return property + "::decimal";
		} else if (Double.class.equals(target)) {
			return property + "::float8";
		} else if (Time.class.isAssignableFrom(target)) {
			return property + "::time";
		} else if (Timestamp.class.isAssignableFrom(target)) {
			return property + "::timestamp";
		} else if (Date.class.isAssignableFrom(target)) {
			return property + "::date";
		} else if (java.util.Date.class.isAssignableFrom(target)) {
			return property + "::timesamp";
		} else {
			// dunno how to cast, leave as is
			return property;
		}
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

		if (function instanceof FilterFunction_date_part) {
			return "date_part";
		} else if (function instanceof FilterFunction_date_trunc) {
			return "date_trunc";
		} else if (function instanceof FilterFunction_to_char) {
			return "to_char";
		}
		return function.getName();
	}

}
