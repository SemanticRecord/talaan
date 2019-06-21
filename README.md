# Talaan
Structured logging in Java over SLF4J

Build status: [![Build Status](https://travis-ci.org/SemanticRecord/talaan.svg?branch=master)](https://travis-ci.org/SemanticRecord/talaan "Travis CI Build")

Talaan is a simple wrapper for [SLF4J](http://www.slf4j.org/) based loggers which adds the ability to
generate log statements by calling Java interface methods instead of having
strings scattered throughout your codebase. In addition, the library promotes
structured, strongly-typed, meaningful logging instead of the more common
unstructured logging used in most projects. Using structured logging instead of
unstructured logging helps your logs to be more easily consumed by tools such as
Splunk and saves you from having to write large amounts of regular expressions to find the log statements.

An example of a typical unstructured log statement might look like the following:

```java
Logger logger = LoggerFactory.getLogger(YourClass.class);
String user = "Rick Grimes";
LocalDate businessDate = LocalDate.of(2016, 7, 1);
String company = "Alexandria";
logger.info("Looking up invoices for user {} on date {} in company {}", 
             user, businessDate, company);
// business logic to perform lookup
```
Logged output will look like the following:
```console
  INFO Looking up invoices for user Rick Grimes on date 2016-07-01 in company Alexandria
```

A structured approach would make it more machine readable: 
```java
logger.info("event={}, user={}, businessDate={}, company={}", "lookupInvoices", 
            user, businessDate, company);
```
Output will be: 
```console
  INFO event=lookupInvoices, user=Rick Grimes, businessDate=2016-07-01, company=Alexandria
```

The preceding example makes logging statements consistent and easier to parse
but we still end up with a bunch of strings scattered over our code base.  We also must remain disciplined across the codebase if we want logging statements which will be easily consumed by our log analysis tools.  If we
had an interface that we could call with strongly typed arguments then the
logging statement and its parameters can be derived automatically. 

```java
public interface BusinessLogger { 
  void lookupInvoices(String user, LocalDate businessDate, String company);
}
BusinessLogger logger = SemanticLogger.getLogger(BusinessLogger.class);
logger.lookupInvoices(user, businessDate, company);
```

The output will be the same as the preceding example.

The advantages you get from such an approach are the following:

* Consistent formatting in log statements
* Easier refactoring
* Logging methods centralized into a single place
* Find callers of the logging statement across your project
* Strong types help promote less defects
* The possibility to mock the logging interface allows for fine-grained verification of the code

## Additional Features

Talaan features an annotation to customize event messages, control logging level, and enrich the 
log statements with specific application codes.  Throwable's stack traces can also be included by 
adding the throwable as the last parameter to the method signature.  For example:

```java
@LogMessage(level = Level.ERROR, eventId = "INVOICEAPP-1001")
void problemSavingRecord(long invoiceId, Stopwatch elapsedTime, Throwable t);
```	

Will be output similarly to:
```console
ERROR event=problemSavingRecord, eventId=INVOICEAPP-1001, invoiceId=1, elapsedTime=61.61 μs
java.lang.RuntimeException: Expected exception message
	at org.semanticrecord.talaan.SemanticLoggerTest.testThrowable(SemanticLoggerTest.java:92)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	...
```

## Usage
To get started with Talaan you may simply add the following dependency to your [Maven](https://maven.apache.org) build:
```xml
		<dependency>
			<groupId>org.semanticrecord</groupId>
			<artifactId>talaan</artifactId>
			<version>0.0.5</version>
		</dependency>
```

This project requires features from Java 8. In order to get
meaningful names for parameters declared on your custom logger interfaces you must
compile with the `-parameters` flag turned on. If using Maven a snippet similar
to the following should suffice:

```xml
	<plugin>
		<groupId>org.apache.maven.plugins</groupId>
		<artifactId>maven-compiler-plugin</artifactId>
		<version>3.5.1</version>
		<configuration>
			<source>1.8</source>
			<target>1.8</target>
			<compilerArgs>
				<arg>-parameters</arg>
			</compilerArgs>
		</configuration>
	</plugin>
```
If you have not configured the parameters argument your logging events will have parameter names like arg0, arg1, etc.

## Future Directions
In addition to simple key-value pairs the Talaan project could provide pluggable options to emit formats such as JSON or other user defined formats.  Feature requests and pull requests are welcome :)

## Similar Work
This project was inspired by the [JBoss Logging Tools](https://developer.jboss.org/wiki/JBossLoggingTooling) project which takes an annotation based approach as well.  It differs from Talaan in that its primary focus seems to be on internationalization (i18n) support.  Talaan fulfills a different use case and requires no code generation.

There is also [LogMachine](https://github.com/UnquietCode/LogMachine) which takes a [Fluent API](https://en.wikipedia.org/wiki/Fluent_interface) approach.  LogMachine also provides extra formatting syntax and an interesting Topic-based logging functionality.

You can read a good background in the [Structured Logging](https://kartar.net/2015/12/structured-logging/) article by James Turnbull.  The article uses JSON as the output encoding for the logging statements, which is a possible future enhancement for Talaan.
