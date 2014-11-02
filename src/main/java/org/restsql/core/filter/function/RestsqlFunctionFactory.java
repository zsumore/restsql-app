package org.restsql.core.filter.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.feature.NameImpl;
import org.geotools.filter.FunctionFactory;
import org.opengis.feature.type.Name;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

public class RestsqlFunctionFactory implements FunctionFactory {

	@Override
	public List<FunctionName> getFunctionNames() {

		List<FunctionName> functionList = new ArrayList<FunctionName>();
		functionList.add(FilterFunction_date_part.NAME);
		functionList.add(FilterFunction_date_trunc.NAME);
		functionList.add(FilterFunction_to_char.NAME);
		functionList.add(FilterFunction_date_format.NAME);
		return Collections.unmodifiableList(functionList);
	}

	@Override
	public Function function(String name, List<Expression> args,
			Literal fallback) {
		return function(new NameImpl(name), args, fallback);
	}

	@Override
	public Function function(Name name, List<Expression> args, Literal fallback) {
		if (FilterFunction_date_part.NAME.getFunctionName().equals(name)) {
			return new FilterFunction_date_part(name, args, fallback);
		} else if (FilterFunction_date_trunc.NAME.getFunctionName()
				.equals(name)) {
			return new FilterFunction_date_trunc(name, args, fallback);
		} else if (FilterFunction_to_char.NAME.getFunctionName().equals(name)) {
			return new FilterFunction_to_char(name, args, fallback);
		}
		return null; // we do not implement that function
	}

}
