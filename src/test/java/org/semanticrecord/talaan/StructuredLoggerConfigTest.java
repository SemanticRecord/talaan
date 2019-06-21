package org.semanticrecord.talaan;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class StructuredLoggerConfigTest {
	
	TestLogger testLogger = TestLoggerFactory.getTestLogger(SampleLoggerInterface.class);
	
	@BeforeClass
	public static void initClass() {
		TestLoggerFactory.getInstance().setPrintLevel(Level.DEBUG);
	}
	
	@After 
	public void setup() {
		StructuredLoggerConfig.reset();
	}

	@Test
	public void getInstanceNoFile() {
		StructuredLoggerConfig config = StructuredLoggerConfig.getInstance();
		assertThat(config.isLoadedFromFile()).isFalse();
	}
	
	@Test
	public void getInstanceIsSingleton() {
		StructuredLoggerConfig config = StructuredLoggerConfig.getInstance();
		StructuredLoggerConfig config2 = StructuredLoggerConfig.getInstance();
		assertThat(config).isSameAs(config2);
	}
	
	@Test
	public void getInstanceWithFile() {
		
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		PrintWriter pWriter = new PrintWriter(bao);
		String expectedValue = "value";
		for(String key : StructuredLoggerConfig.getPropertyKeys()) {
			pWriter.format("%s=%s\n", key, expectedValue);			
		}
		pWriter.flush();
		pWriter.close();
		ClassLoader contextCl = Thread.currentThread().getContextClassLoader();
		try {
			
			ClassLoader cl = new ClassLoader(contextCl) {

				@Override
				public InputStream getResourceAsStream(String name) {
					if(name.equals(StructuredLoggerConfig.PROPERTIES_FILE)) {
						return new ByteArrayInputStream(bao.toByteArray());
					}
					return super.getResourceAsStream(name);
				}
				
			};
			Thread.currentThread().setContextClassLoader(cl);
			StructuredLoggerConfig config = StructuredLoggerConfig.getInstance();
			assertThat(config.isLoadedFromFile()).isTrue();
			assertThat(config.getEventId()).isEqualTo(expectedValue);
			assertThat(config.getEvent()).isEqualTo(expectedValue);
			assertThat(config.getPairFormat()).isEqualTo(expectedValue);
			assertThat(config.getPlaceholder()).isEqualTo(expectedValue);
			assertThat(config.getSeparator()).isEqualTo(expectedValue);
		} finally {
			Thread.currentThread().setContextClassLoader(contextCl);
		}
	}

}
