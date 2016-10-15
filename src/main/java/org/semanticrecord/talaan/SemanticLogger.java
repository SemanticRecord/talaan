/**
 *
 */
package org.semanticrecord.talaan;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticrecord.talaan.LogMessage.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a way to wrap an SLF4J Logger with an object implementing an
 * interface you supply and which generates log entries based on the methods
 * called on that interface.
 * 
 * @author Rex Sheridan
 *
 */
public class SemanticLogger {

	/**
	 * Uses the logInterface class as the name of the logger.
	 * 
	 * @param logInterface
	 *            to interface for which we will generate logging statements
	 * @param <T>
	 *            the interface to be implemented by the generated proxy in
	 *            order to log events
	 * @return a proxied implementation of logInterface which logs invocations
	 */
	public static <T> T getLogger(Class<T> logInterface) {
		return getLogger(logInterface, logInterface);
	}

	/**
	 * Allows the logging interface to be reused by multiple classes while each
	 * class maintains their own logger name.
	 * 
	 * @param logInterface
	 *            returned proxy will adhere this interface
	 * @param loggerName the logger category to be passed to the SLFJ LoggerFactory
	 * @param <T>
	 *            the interface to be implemented by the generated proxy in
	 *            order to log events
	 * @return a proxied implementation of logInterface which logs invocations
	 */
	public static <T> T getLogger(Class<T> logInterface, Class<?> loggerName) {
		return getLogger(logInterface, loggerName.getName());
	}

	public static <T> T getLogger(Class<T> logInterface, String loggerName) {

		Method[] declaredMethods = logInterface.getDeclaredMethods();
		List<Method> declaredMethodsList = Arrays.asList(declaredMethods);
		ClassLoader classLoader = logInterface.getClassLoader();
		Class<?>[] interfaces = new Class<?>[] { logInterface };
		Logger log = LoggerFactory.getLogger(loggerName);

		Map<Method, InvocationHandler> methodMap = new HashMap<>();
		for (Method m : declaredMethodsList) {
			methodMap.put(m, createLoggingHandler(log, m));
		}

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
		String logMessage = generateFormatString(eventName, message.eventId(), parametersList);
		BiConsumer<String, Object[]> biCons = getLevelMethod(log, message.level());

		InvocationHandler h = (Object proxy, Method m2, Object[] args) -> {
			biCons.accept(logMessage, args);
			return Void.TYPE;
		};
		return h;
	}

	@LogMessage
	private static void defaultMessage() {
	}

	private static LogMessage getDefaultMessageAnnotation() {
		try {
			Method defaultMethod = SemanticLogger.class.getDeclaredMethod("defaultMessage");
			return defaultMethod.getAnnotation(LogMessage.class);
		} catch (Exception e) {
			throw new RuntimeException("Could not access private method of our own class", e);
		}
	}

	private static String generateFormatString(String eventName, String code, List<Parameter> parametersList) {
		SemanticLoggerConfig config = SemanticLoggerConfig.getInstance();
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

	private static BiConsumer<String, Object[]> getLevelMethod(final Logger log, Level level) {
		BiConsumer<String, Object[]> biCons = log::error;
		if (level == Level.DEBUG) {
			biCons = log::debug;
		} else if (level == Level.INFO) {
			biCons = log::info;
		} else if (level == Level.WARN) {
			biCons = log::warn;
		}
		return biCons;
	}
}
