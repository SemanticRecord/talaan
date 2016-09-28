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

	void lookingUpInvoices(String user, LocalDate businessDate, String businessName);

	void foundAccountInvoices(String user, long recordsRetrieved, Stopwatch elapsedTime);

	@LogMessage("saved invoice summary")
	void updatedInvoice(String invoiceTitle, long invoiceId, int detailsRowsCount, Stopwatch elapsedTime);

	@LogMessage(level = Level.ERROR, code = "INVOICEAPP-1001")
	void problemSavingRecord(long invoiceId, Stopwatch elapsedTime, Throwable t);


}