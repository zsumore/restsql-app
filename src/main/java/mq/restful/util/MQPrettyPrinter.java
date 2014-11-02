package mq.restful.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

public class MQPrettyPrinter extends MinimalPrettyPrinter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1970489595966291458L;

	@Override
	public void writeArrayValueSeparator(JsonGenerator jg) throws IOException,
			JsonGenerationException {
		super.writeArrayValueSeparator(jg);
		jg.writeRaw("\n");
	}

}
