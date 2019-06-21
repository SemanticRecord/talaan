/*
 * Copyright 2016 Rex Sheridan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.semanticrecord.talaan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import org.slf4j.Logger;

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
		DEBUG((logger) -> logger.isDebugEnabled()), 
		INFO((logger) -> logger.isInfoEnabled()), 
		WARN((logger) -> logger.isWarnEnabled()), 
		ERROR((logger) -> logger.isErrorEnabled());
		private final Predicate<Logger> enabledPredicate;
		private Level(Predicate<Logger> fn) {
			this.enabledPredicate = fn;
		}
		
		public Predicate<Logger> getEnabledPredicate() {
			return enabledPredicate;
		}
		
		public boolean isEnabled(Logger logger) {
			return enabledPredicate.test(logger);
		}
	};

	/**
	 * Specifies the event name. Defaults to the method name used by the
	 * interface
	 * @return the event name
	 */
	String value() default "";

	/**
	 * Specifies the log level to use
	 * @return the logging level used to log the statement
	 */
	Level level() default Level.INFO;

	/**
	 * Allows for a specific message code to be applied to each message
	 * @return the event_id to attach to the log entry
	 */
	String eventId() default "";
}
