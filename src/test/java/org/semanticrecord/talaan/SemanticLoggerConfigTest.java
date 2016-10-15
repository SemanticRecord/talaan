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

public class SemanticLoggerConfigTest {
	
	TestLogger testLogger = TestLoggerFactory.getTestLogger(SampleLoggerInterface.class);
	
	@BeforeClass
	public static void initClass() {
		TestLoggerFactory.getInstance().setPrintLevel(Level.DEBUG);
	}
	
	@After 
	public void setup() {
		SemanticLoggerConfig.reset();
	}

	@Test
	public void getInstanceNoFile() {
		SemanticLoggerConfig config = SemanticLoggerConfig.getInstance();
		assertThat(config.isLoadedFromFile()).isFalse();
	}
	
	@Test
	public void getInstanceWithFile() {
		
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		PrintWriter pWriter = new PrintWriter(bao);
		String expectedValue = "value";
		for(String key : SemanticLoggerConfig.getPropertyKeys()) {
			pWriter.format("%s=%s\n", key, expectedValue);			
		}
		pWriter.flush();
		pWriter.close();
		ClassLoader contextCl = Thread.currentThread().getContextClassLoader();
		try {
			
			ClassLoader cl = new ClassLoader(contextCl) {

				@Override
				public InputStream getResourceAsStream(String name) {
					if(name.equals(SemanticLoggerConfig.PROPERTIES_FILE)) {
						return new ByteArrayInputStream(bao.toByteArray());
					}
					return super.getResourceAsStream(name);
				}
				
			};
			Thread.currentThread().setContextClassLoader(cl);
			SemanticLoggerConfig config = SemanticLoggerConfig.getInstance();
			assertThat(config.isLoadedFromFile()).isTrue();
			assertThat(config.getCode()).isEqualTo(expectedValue);
			assertThat(config.getEvent()).isEqualTo(expectedValue);
			assertThat(config.getPairFormat()).isEqualTo(expectedValue);
			assertThat(config.getPlaceholder()).isEqualTo(expectedValue);
			assertThat(config.getSeparator()).isEqualTo(expectedValue);
		} finally {
			Thread.currentThread().setContextClassLoader(contextCl);
		}
	}

}
