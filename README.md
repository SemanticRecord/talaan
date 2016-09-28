# Talaan
Semantic logging in Java over SLF4J

Build status: [![Build Status] (https://travis-ci.org/SemanticRecord/talaan.svg?branch=master)](https://travis-ci.org/SemanticRecord/talaan "Travis CI Build")

Talaan is a simple wrapper for SLF4J based loggers which adds the ability to generate log statements by calling Java interface methods instead of having strings scattered throughout your codebase.  In addition, the library promotes sturctured, strongly-typed, semantic logging instead of the more common unstructured logging used in most projects.  Using semantic logging instead of unstructured logging helps your logs to be more easily consumed by tools such as Splunk.

An example of a typical unstructured log statement might look like the following:

```java
Logger logger = LoggerFactory.getLogger(SampleLoggerInterface.class);
String user = "Rick Grimes";
LocalDate businessDate = LocalDate.of(2016, 7, 1);
String company = "Alexandria";
logger.info("Looking up invoices for user {} on date {} in company {}", user, businessDate, company);
// business logic to perform lookup
```
Logged output will look like the following:
```console
  INFO Looking up invoices for user Rick Grimes on date 2016-07-01 in company Alexandria.
```

A semantic approach would make it more machine readable:
```java
logger.info("event={}, user={}, businessDate={}, company={}", "lookupInvoices", user, businessDate, company);
```
Output will be:
```console
  INFO event=lookupInvoices, user=Rick Grimes, businessDate=2016-07-01, company=Alexandria.
```
The preceding example makes logging statements consistent and easier to parse but we still end up with a bunch of strings scattered over our code base.  If we had an interface that we could call with strongly typed arguments then the logging statement and its parameters can be derived automatically.
```java
public interface BusinessLogger { 
  void lookupInvoices(String user, LocalDate businessDate, String company);
}

BusinessLogger logger = SemanticLogger.getLogger(BusinessLogger.class);
logger.lookupInvoices(user, businessDate, company);
```

The output will be the same as the preceding example but easier to maintain.

The advantages you get from such an approach are the following:

* Consistent formatting in log statements
* Easier refactoring
* Logging methods centralized into a single place
* Find callers of the logging statement
* Strong types help promote less defects
* The possibility to mock the logging interface allows for fine-grained verification of the code
