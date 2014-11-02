package org.restsql.core.filter.function;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.sql.Timestamp;
import java.util.List;

import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.feature.type.Name;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

public class FilterFunction_date_format extends FunctionExpressionImpl {
	public static FunctionName NAME = new FunctionNameImpl("date_format",
			Integer.class, parameter("timestamp", Timestamp.class), parameter(
					"format", String.class));

	public FilterFunction_date_format() {
		super(NAME);
	}

	public FilterFunction_date_format(Name name, List<Expression> args,
			Literal fallback) {

		super(NAME);
		this.setFallbackValue(fallback);
		this.setParameters(args);

	}
}
