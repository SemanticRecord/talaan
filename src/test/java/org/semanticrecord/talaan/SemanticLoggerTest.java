package org.semanticrecord.talaan;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.util.function.Consumer;

import org.junit.Test;

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
public class SemanticLoggerTest {

	TestLogger testLogger = TestLoggerFactory.getTestLogger(SampleLoggerInterface.class);

	@Test
	public void test() {
		Stopwatch elapsedTime = Stopwatch.createStarted();
		SampleLoggerInterface logger = SemanticLogger.getLogger(SampleLoggerInterface.class);

		logger.loggerCreated(elapsedTime.stop());
		String msg = "event=loggerCreated, elapsedTime={}";

		assertSingleEvent(event -> {
			String eventMessage = event.getMessage();
			assertThat(event.getLevel(), is(Level.INFO));
			assertThat(event.getCreatingLogger().getName(), is(SampleLoggerInterface.class.getName()));
			assertThat(eventMessage, is(msg));
			ImmutableList<Object> args = event.getArguments();
			assertThat(args.size(), is(1));
			assertThat(args.get(0), is(elapsedTime));
		});

		String user = "rex";
		LocalDate businessDate = LocalDate.now();
		String businessName = "ACME Corp";
		logger.lookingUpInvoices(user, businessDate, businessName);

		elapsedTime.reset().start();
		logger.foundAccountInvoices(user, 123, elapsedTime.stop());

		elapsedTime.reset().start();
		String invoiceTitle = "2016-07-01 Full Invoice";
		logger.updatedInvoice(invoiceTitle, 456, 10, elapsedTime.stop());

		elapsedTime.reset().start();
		try {
			throw new RuntimeException("Expected exception message");
		} catch (Exception e) {
			logger.problemSavingRecord(1, elapsedTime.stop(), e);
		}
		ImmutableList<LoggingEvent> allLoggingEvents = testLogger.getAllLoggingEvents();

		allLoggingEvents.forEach(event -> System.out.println(event));
	}

	private void assertSingleEvent(Consumer<LoggingEvent> consumer) {
		ImmutableList<LoggingEvent> events = testLogger.getLoggingEvents();
		assertThat(events.size(), is(1));
		consumer.accept(events.get(0));
		testLogger.clear();

	}

}
