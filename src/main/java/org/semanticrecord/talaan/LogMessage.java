/**
 *
 */
package org.semanticrecord.talaan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate a method on a logging interface and customize the output of
 * the log entry.
 * 
 * @author Rex Sheridan
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface LogMessage {

	public enum Level {
		DEBUG, INFO, WARN, ERROR
	};

	/**
	 * Specifies the event name. Defaults to the method name used by the
	 * interface
	 */
	String value() default "";

	/**
	 * Specifies the log level to use
	 */
	Level level() default Level.INFO;

	/**
	 * Allows for a specific message code to be applied to each message
	 */
	String code() default "";
}
