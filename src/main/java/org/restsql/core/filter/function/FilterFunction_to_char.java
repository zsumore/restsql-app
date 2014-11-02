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

public class FilterFunction_to_char extends FunctionExpressionImpl {

	public static FunctionName NAME = new FunctionNameImpl("to_char",
			String.class, parameter("timestamp", Timestamp.class), parameter("text",
					String.class));

	public FilterFunction_to_char() {
		super(NAME);
	}

	public FilterFunction_to_char(Name name, List<Expression> args,
			Literal fallback) {

		super(NAME);
		this.setFallbackValue(fallback);
		this.setParameters(args);

	}

}
