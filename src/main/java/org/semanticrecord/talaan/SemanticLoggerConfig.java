/**
 * 
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
 * @author Rex Sheridan <rex.sheridan@semanticrecord.org>
 *
 */
public final class SemanticLoggerConfig {
	private static final Logger log = LoggerFactory.getLogger(SemanticLoggerConfig.class);

	public static final String PROPERTIES_FILE = "talaan.properties";
	public static final String LOG_PLACEHOLDER = "{}";
	public static final String PAIR_FORMAT_DEFAULT = "%s=%s";
	public static final String CODE_NAME_DEFAULT = "code";
	public static final String EVENT_NAME_DEFAULT = "event";
	public static final String SEPARATOR_DEFAULT = ", ";

	public static final String PREFIX_LOG_FORMAT = "log_format.";
	public static final String PROP_LOG_PLACE_HOLDER = PREFIX_LOG_FORMAT + "placeholder";
	public static final String PROP_PAIR_FORMAT = PREFIX_LOG_FORMAT + "pair_format";
	public static final String PROP_CODE_NAME = PREFIX_LOG_FORMAT + "code_name";
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
		List<String> propsList = Arrays.asList(PROP_CODE_NAME, PROP_EVENT_NAME, PROP_LOG_PLACE_HOLDER, PROP_PAIR_FORMAT,
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

	public String getCode() {
		return props.getOrDefault(PROP_CODE_NAME, CODE_NAME_DEFAULT);
	}

	@Override
	public String toString() {
		return String.format("SemanticLoggerConfig [props=%s, loadedFromFile=%s]", props, loadedFromFile);
	}

}
