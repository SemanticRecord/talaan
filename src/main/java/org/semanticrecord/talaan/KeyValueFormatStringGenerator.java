/**
 * 
 */
package org.semanticrecord.talaan;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Rex Sheridan
 *
 */
public class KeyValueFormatStringGenerator implements FormatStringGenerator {

	@Override
	public String generateFormatString(String eventName, String code, List<Parameter> parametersList) {
		StructuredLoggerConfig config = StructuredLoggerConfig.getInstance();
		String format = config.getPairFormat();
		
		String logMessage = Stream.concat(
		Stream.<String>builder()
		.add(formatPair(format, config.getEvent(), eventName))
		.add(code.isEmpty() ? "" : formatPair(format, config.getEventId(), code))
		.build(),
		parametersList.stream()
		.filter(p ->  !Throwable.class.isAssignableFrom(p.getType()))
		.map(p -> formatPair(format, p.getName(), config.getPlaceholder())))
		.filter(part -> !part.isEmpty())
		.collect(Collectors.joining(config.getSeparator()));
		
		return logMessage;
	}
	
	private static String formatPair(String format, Object o1, Object o2) {
		return String.format(format, o1, o2);
	}

}
