package org.semanticrecord.talaan;

import java.time.LocalDate;

import org.semanticrecord.talaan.LogMessage.Level;

import com.google.common.base.Stopwatch;

/**
 * @author Rex Sheridan
 *
 */
public interface SampleLoggerInterface {
	void loggerCreated(Stopwatch elapsedTime);

	void lookupInvoices(String user, LocalDate businessDate, String company);

	void foundAccountInvoices(String user, long recordsRetrieved, Stopwatch elapsedTime);

	@LogMessage("saved invoice summary")
	void updatedInvoice(String invoiceTitle, long invoiceId, int detailsRowsCount, Stopwatch elapsedTime);

	@LogMessage(level = Level.ERROR, eventId = "INVOICEAPP-1001")
	void problemSavingRecord(long invoiceId, Stopwatch elapsedTime, Throwable t);


}