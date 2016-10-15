/**
 *
 */
package org.semanticrecord.talaan;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.semanticrecord.talaan.LogMessage.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rex Sheridan
 *
 */
public class SemanticLogger {

	public static final String LOG_PLACEHOLDER = "{}";
	public static final String CODE_PARAM_DEFAULT = "code";
	public static final String PAIR_FORMAT_DEFAULT = "%s=%s";
	public static final String EVENT_DEFAULT = "event";
	public static final String SEPARATOR_DEFAULT = ", ";

	/**
	 * Uses the logInterface class as the name of the logger.
	 * @param logInterface to interface for which we will generate logging statements
	 * @return a proxied implementation of logInterface which logs invocations 
	 */
	public static <T> T getLogger(Class<T> logInterface) {
		return getLogger(logInterface, logInterface);
	}

	/**
	 * @param logInterface
	 * @param loggerName
	 * @return
	 */
	public static <T> T getLogger(Class<T> logInterface, Class<?> loggerName) {
		return getLogger(logInterface, loggerName.getName());
	}

	public static <T> T getLogger(Class<T> logInterface, String loggerName) {
		
		Method[] declaredMethods = logInterface.getDeclaredMethods();
		List<Method> declaredMethodsList = Arrays.asList(declaredMethods);
		ClassLoader classLoader = logInterface.getClassLoader();
		Class<?>[] interfaces = new Class<?>[] {logInterface};
		Logger log = LoggerFactory.getLogger(loggerName);

		Map<Method, InvocationHandler> methodMap = declaredMethodsList.stream()
				.collect(Collectors.toMap(m -> m, m -> createLoggingHandler(log, m)));
		InvocationHandler h = createDispatchingHandler(methodMap);

		@SuppressWarnings("unchecked")
		T proxy = (T) Proxy.newProxyInstance(classLoader, interfaces, h);
		return proxy;
	}

	private static InvocationHandler createDispatchingHandler(Map<Method, InvocationHandler> methodMap) {
		InvocationHandler h = (Object proxy, Method method, Object[] args) -> {
			return methodMap.get(method).invoke(proxy, method, args);
		};
		return h;
	}

	private static InvocationHandler createLoggingHandler(final Logger log, Method method) {
		LogMessage defaultMessage = getDefaultMessageAnnotation();
		LogMessage message = Optional.ofNullable(method.getAnnotation(LogMessage.class)).orElse(defaultMessage);

		String eventName = message.value().isEmpty() ? method.getName() : message.value();
		Parameter[] parameters = method.getParameters();
		List<Parameter> parametersList = Arrays.asList(parameters);
		String logMessage = generateFormatString(eventName, message.code(), parametersList);
		BiConsumer<String, Object[]> biCons = getLevelMethod(log, message.level());

		InvocationHandler h = (Object proxy, Method m2, Object[] args) -> {
			biCons.accept(logMessage, args);
			return Void.TYPE;
		};
 		return h;
	}

	@LogMessage
	private static void defaultMessage() {}

	private static LogMessage getDefaultMessageAnnotation() {
		try {
			Method defaultMethod = SemanticLogger.class.getDeclaredMethod("defaultMessage");
			return defaultMethod.getAnnotation(LogMessage.class);
		} catch (Exception e) {
			throw new RuntimeException("Could not access private method of our own class", e);
		}
	}

	private static String generateFormatString(String eventName, String code, List<Parameter> parametersList) {
		List<String> allParts = new ArrayList<>();
		SemanticLoggerConfig config = SemanticLoggerConfig.getInstance();
		String format = config.getPairFormat();
		allParts.add(formatPair(format, config.getEvent(), eventName));
		if(!code.isEmpty()) {
			allParts.add(formatPair(format, config.getCode(), code));
		}
		List<String> formattedParams = parametersList.stream()
										.filter(p -> !Throwable.class.isAssignableFrom(p.getType()))
										.map(p -> formatPair(format, p.getName(), config.getPlaceholder()))
										.collect(Collectors.toList());
		allParts.addAll(formattedParams);

		String logMessage = join(config.getSeparator(), allParts);
		return logMessage;
	}

	private static String join(String separator, Iterable<String> partsIterable) {
		Iterator<String> parts = partsIterable.iterator();
		StringBuilder builder = new StringBuilder();
		if (parts.hasNext()) {
		      builder.append(parts.next().toString());
		      while (parts.hasNext()) {
		        builder.append(separator);
		        builder.append(parts.next().toString());
		      }
		    }
		return builder.toString();
	}

	private static String formatPair(String format, Object o1, Object o2) {
		return String.format(format, o1, o2);
	}

	private static BiConsumer<String, Object[]> getLevelMethod(final Logger log, Level level) {
		BiConsumer<String, Object[]> biCons = log::error;
		if(level == Level.DEBUG) {
			biCons = log::debug;
		} else if(level == Level.INFO) {
			biCons = log::info;
		} else if(level == Level.WARN) {
			biCons = log::warn;
		}
		return biCons;
	}
}
