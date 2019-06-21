package org.semanticrecord.talaan;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;

import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;


/**
 * @author Rex Sheridan
 *
 */
public class StructuredLoggerFactoryTest {

	TestLogger testLogger = TestLoggerFactory.getTestLogger(SampleLoggerInterface.class);
	SampleLoggerInterface logger = StructuredLoggerFactory.getLogger(SampleLoggerInterface.class);
	
	Stopwatch elapsedTime;
	
	@BeforeClass
	public static void initClass() {
		TestLoggerFactory.getInstance().setPrintLevel(Level.INFO);
	}
	
	@Before
	public void setup() {
		elapsedTime = Stopwatch.createStarted();
	}
	
	@After
	public void teardown() {
		testLogger.clearAll();
	}
	
	@Test
	public void jsonOutput() {
		JsonBuilderFactory factory = Json.createBuilderFactory(Collections.emptyMap());
		JsonObject value = factory.createObjectBuilder()
		     .add("firstName", "John")
		     .add("lastName", "Smith")
		     .add("age", 25)
		     .add("address", factory.createObjectBuilder()
		         .add("streetAddress", "21 2nd Street")
		         .add("city", "New York")
		         .add("state", "NY")
		         .add("postalCode", "10021"))
		     .add("phoneNumber", factory.createArrayBuilder()
		         .add(factory.createObjectBuilder()
		             .add("type", "home")
		             .add("number", "212 555-1234"))
		         .add(factory.createObjectBuilder()
		             .add("type", "fax")
		             .add("number", "646 555-4567")))
		     .build();
		logger.builtJsonObject(value.toString());
	}

	@Test
	public void simpleLogEntry() {
		logger.loggerCreated(elapsedTime.stop());
	
		assertSingleEvent(event -> {
			
			assertThat(event.getLevel()).isEqualTo(Level.INFO);
			assertThat(event.getCreatingLogger().getName()).isEqualTo(SampleLoggerInterface.class.getName());
			String msg = "event=loggerCreated, elapsedTime={}";
			String eventMessage = event.getMessage();
			assertThat(eventMessage).isEqualTo(msg);
			ImmutableList<Object> args = event.getArguments();
			assertThat(args.size()).isEqualTo(1);
			assertThat(args.get(0)).isEqualTo(elapsedTime);
		});
	}

	@Test
	public void variousCases() {
		String user = "Rick Grimes";
		LocalDate businessDate = LocalDate.of(2016, 7, 1);
		String businessName = "ACME Corp";
		
		logger.lookupInvoices(user, businessDate, businessName);
		
		assertSingleEvent(event -> {
			String msg = "event=lookupInvoices, user={}, businessDate={}, company={}";
			String eventMessage = event.getMessage();
			assertThat(eventMessage).isEqualTo(msg);
		});

		elapsedTime.reset().start();
		logger.foundAccountInvoices(user, 123, elapsedTime.stop());

		elapsedTime.reset().start();
		String invoiceTitle = "2016-07-01 Full Invoice";
		logger.updatedInvoice(invoiceTitle, 456, 10, elapsedTime.stop());
		testLogger.clear();
	}

	@Test
	public void testThrowable() {
		elapsedTime.reset().start();
		RuntimeException ex = new RuntimeException("Expected exception message");
		try {
			throw ex;
		} catch (Exception e) {
			logger.problemSavingRecord(1, elapsedTime.stop(), e);
		}
		assertSingleEvent(event -> {
			String msg = "event=problemSavingRecord, eventId=INVOICEAPP-1001, invoiceId={}, elapsedTime={}";
			String eventMessage = event.getMessage();
			assertThat(eventMessage).isEqualTo(msg);
			
			assertThat(event.getLevel()).isEqualTo(Level.ERROR);
			
			Optional<Throwable> throwableOpt = event.getThrowable();
			assertThat(throwableOpt.isPresent()).isEqualTo(true);
			assertThat(throwableOpt.get()).isEqualTo(ex);
		});
	}

	private void assertSingleEvent(Consumer<LoggingEvent> consumer) {
		ImmutableList<LoggingEvent> events = testLogger.getLoggingEvents();
		assertThat(events.size()).isEqualTo(1);
		consumer.accept(events.get(0));
		testLogger.clear();
	}

}
