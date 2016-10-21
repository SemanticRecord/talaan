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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the logger with a set of parameters to control the format of logging
 * statements. If a file named <code>talaan.properties</code> is supplied on the
 * classpath the values from that file will be used to override the format.
 * 
 * @author Rex Sheridan
 *
 */
public final class SemanticLoggerConfig {
	private static final Logger log = LoggerFactory.getLogger(SemanticLoggerConfig.class);

	public static final String PROPERTIES_FILE = "talaan.properties";
	public static final String LOG_PLACEHOLDER = "{}";
	public static final String PAIR_FORMAT_DEFAULT = "%s=%s";
	public static final String EVENT_ID_DEFAULT = "eventId";
	public static final String EVENT_NAME_DEFAULT = "event";
	public static final String SEPARATOR_DEFAULT = ", ";

	public static final String PREFIX_LOG_FORMAT = "log_format.";
	public static final String PROP_LOG_PLACE_HOLDER = PREFIX_LOG_FORMAT + "placeholder";
	public static final String PROP_PAIR_FORMAT = PREFIX_LOG_FORMAT + "pair_format";
	public static final String PROP_EVENT_ID = PREFIX_LOG_FORMAT + "event_id";
	public static final String PROP_EVENT_NAME = PREFIX_LOG_FORMAT + "event_name";
	public static final String PROP_SEPARATOR = PREFIX_LOG_FORMAT + "separator";

	private static AtomicReference<SemanticLoggerConfig> ref = new AtomicReference<>();

	private final Map<String, String> props;

	private final boolean loadedFromFile;

	private SemanticLoggerConfig(Map<String, String> props, boolean loadedFromFile) {
		this.props = props;
		this.loadedFromFile = loadedFromFile;
	}

	public static SemanticLoggerConfig getInstance() {
		SemanticLoggerConfig config = ref.get();
		if (config != null) {
			return config;
		}
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		boolean loadedFromFile = false;
		try (InputStream inputStream = classLoader.getResourceAsStream(PROPERTIES_FILE)) {
			if (inputStream != null) {
				props.load(inputStream);
				loadedFromFile = true;
			}
		} catch (IOException e) {
			log.error("{}=Could not load properties, file={}", EVENT_NAME_DEFAULT, PROPERTIES_FILE);
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, String> propsMap = ((Map) props);
		SemanticLoggerConfig newConfig = new SemanticLoggerConfig(Collections.unmodifiableMap(propsMap),
				loadedFromFile);
		boolean refWasSet = ref.compareAndSet(null, newConfig);
		config = ref.get();
		log.debug("{}=create config, refWasSet={}, loadedFromFile={}", config.getEvent(), refWasSet, loadedFromFile);
		return config;
	}

	public static void reset() {
		log.debug("{}=reset", EVENT_NAME_DEFAULT);
		ref.set(null);
	}

	public boolean isLoadedFromFile() {
		return loadedFromFile;
	}

	public static Set<String> getPropertyKeys() {
		List<String> propsList = Arrays.asList(PROP_EVENT_ID, PROP_EVENT_NAME, PROP_LOG_PLACE_HOLDER, PROP_PAIR_FORMAT,
				PROP_SEPARATOR);
		Set<String> propsSet = new HashSet<>(propsList);
		return Collections.unmodifiableSet(propsSet);
	}

	public Map<String, String> asMap() {
		return props;
	}

	public String getPlaceholder() {
		return props.getOrDefault(PROP_LOG_PLACE_HOLDER, LOG_PLACEHOLDER);
	}

	public String getPairFormat() {
		return props.getOrDefault(PROP_PAIR_FORMAT, PAIR_FORMAT_DEFAULT);
	}

	public String getEvent() {
		return props.getOrDefault(PROP_EVENT_NAME, EVENT_NAME_DEFAULT);
	}

	public String getSeparator() {
		return props.getOrDefault(PROP_SEPARATOR, SEPARATOR_DEFAULT);
	}

	public String getEventId() {
		return props.getOrDefault(PROP_EVENT_ID, EVENT_ID_DEFAULT);
	}

	@Override
	public String toString() {
		return String.format("SemanticLoggerConfig [props=%s, loadedFromFile=%s]", props, loadedFromFile);
	}

}
