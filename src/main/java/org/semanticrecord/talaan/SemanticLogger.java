/**
 *
 */
package org.semanticrecord.talaan;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.semanticrecord.talaan.LogMessage.Level;

/**
 * @author Rex Sheridan
 *
 */
public class SemanticLogger {

	public static final String LOG_PLACEHOLDER = "{}";
	public static final String CODE_PARAM_DEFAULT = "code";
	public static final String PAIR_FORMAT_DEFAULT = "%s=%s";
	public static final String METHOD_DEFAULT = "event";
	public static final String SEPARATOR_DEFAULT = ", ";

	public static <T> T getLogger(Class<T> logInterface) {
		return getLogger(logInterface, logInterface);
	}

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
				.collect(Collectors.toMap(m -> m, m -> createHandler(log, m)));
		InvocationHandler h = (Object proxy, Method m2, Object[] args) -> {
			return methodMap.get(m2).invoke(proxy, m2, args);
		};


		@SuppressWarnings("unchecked")
		T proxy = (T) Proxy.newProxyInstance(classLoader, interfaces, h);
		return proxy;
	}

	@LogMessage
	private static void defaultMessage() {}

	private static InvocationHandler createHandler(final Logger log, Method method) {
		LogMessage defaultMessage = getDefaultMessageAnnotation();
		LogMessage message = Optional.ofNullable(method.getAnnotation(LogMessage.class)).orElse(defaultMessage);

		String eventName = message.value().isEmpty() ? method.getName() : message.value();
		Parameter[] parameters = method.getParameters();
		List<Parameter> parametersList = Arrays.asList(parameters);
		String logMessage = generateFormatString(message.code(), eventName, parametersList);
		BiConsumer<String, Object[]> biCons = getLevelMethod(log, message.level());

		InvocationHandler h = (Object proxy, Method m2, Object[] args) -> {
			biCons.accept(logMessage, args);
			return Void.TYPE;
		};
 		return h;
	}

	private static LogMessage getDefaultMessageAnnotation() {
		try {
			Method defaultMethod = SemanticLogger.class.getDeclaredMethod("defaultMessage");
			return defaultMethod.getAnnotation(LogMessage.class);
		} catch (Exception e) {
			throw new RuntimeException("Could not access private method of our own class", e);
		}
	}

	private static String generateFormatString(String code, String methodName, List<Parameter> parametersList) {

		Stream<String> codeStream = code.isEmpty() ? Stream.empty() : Stream.of(format(CODE_PARAM_DEFAULT, code));
		Stream<String> formattedParams = parametersList.stream()
		.filter(p -> !Throwable.class.isAssignableFrom(p.getType()))
		.map(p -> format(p.getName(), LOG_PLACEHOLDER));

		List<String> allParts =
				Stream.concat(codeStream,
				Stream.concat(
						Stream.of(format(METHOD_DEFAULT, methodName)),
						formattedParams))
				.collect(Collectors.toList());

		String logMessage = join(SEPARATOR_DEFAULT, allParts);
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

	private static String format(Object o1, Object o2) {
		String fmt = PAIR_FORMAT_DEFAULT;
		return String.format(fmt, o1, o2);
	}

	private static BiConsumer<String, Object[]> getLevelMethod(final Logger log, Level level) {
		BiConsumer<String, Object[]> biCons = null;
		if(level == Level.DEBUG) {
			biCons = log::debug;
		} else if(level == Level.INFO) {
			biCons = log::info;
		} else if(level == Level.WARN) {
			biCons = log::warn;
		} else {
			biCons = log::error;
		}
		return biCons;
	}
}
