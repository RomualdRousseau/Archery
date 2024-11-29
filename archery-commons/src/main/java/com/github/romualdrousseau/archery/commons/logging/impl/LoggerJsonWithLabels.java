package com.github.romualdrousseau.archery.commons.logging.impl;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

import com.github.romualdrousseau.archery.commons.logging.LoggerLabels;
import com.github.romualdrousseau.archery.commons.logging.LoggerWithLabels;

import static net.logstash.logback.argument.StructuredArguments.kv;

public class LoggerJsonWithLabels implements LoggerWithLabels {

    private final Logger logger;
    private LoggerLabels labels;

    public LoggerJsonWithLabels(final Logger logger) {
        this.logger = logger;
        this.labels = new LoggerLabels();
    }

    /**
     * Sets the labels for the LoggerJsonWithLabels object.
     * The labels define the categories or tags that can be assigned to log messages.
     *
     * @param labels the LoggerLabels object containing the labels to be set
     * @return the updated LoggerJsonWithLabels object with the new labels
     */
    public LoggerJsonWithLabels setLabels(final LoggerLabels labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Returns the name of the logger associated with this instance.
     *
     * @return the name of the logger
     */
    @Override
    public String getName() {
        return this.logger.getName();
    }

    /**
     * Check if the trace level logging is enabled for this logger.
     * This method delegates the call to the underlying logger and returns the value.
     *
     * @return true if trace level logging is enabled, false otherwise.
     */
    @Override
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    /**
     * Logs a trace message with the given message and labels.
     *
     * @param msg the message to be logged
     */
    @Override
    public void trace(final String msg) {
        this.logger.trace(msg, this.labels.get());
    }

    /**
     * Traces a log message with a single argument.
     *
     * This method formats the log message using the specified format string
     * and the argument provided. It then calls the trace method with the
     * formatted log message as a parameter.
     *
     * @param format - the format string for the log message
     * @param arg - the argument for the log message
     */
    @Override
    public void trace(final String format, final Object arg) {
        this.trace(MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * Logs a trace message with the specified format and arguments.
     *
     * @param format - the format string to be used for the trace message
     * @param arg1 - the first argument to be formatted and included in the trace message
     * @param arg2 - the second argument to be formatted and included in the trace message
     */
    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        this.trace(MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * Prints a trace message with the provided format and arguments.
     * The trace message is created by formatting the format string with the provided arguments.
     *
     * @param format the format string for the trace message
     * @param arguments the arguments to be used in formatting the trace message
     */
    @Override
    public void trace(final String format, final Object... arguments) {
        this.trace(MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Traces a log message along with an exception in the code.
     *
     * @param msg the log message to be traced
     * @param t the Throwable object representing the exception
     */
    @Override
    public void trace(final String msg, final Throwable t) {
        this.logger.trace(msg, this.labels.get(), kv("exception", t));
    }

    /**
     * Determines if the trace level is enabled for a given marker.
     *
     * @param marker The marker for which to check if the trace level is enabled.
     * @return True if the trace level is enabled for the given marker, false otherwise.
     */
    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return this.logger.isTraceEnabled(marker);
    }

    /**
     * Logs a message with TRACE level, including a specified marker, using the logger associated with this class.
     *
     * @param marker The marker associated with the log message.
     * @param msg The log message to be logged.
     *
     */
    @Override
    public void trace(final Marker marker, final String msg) {
        this.logger.trace(marker, msg, this.labels.get());
    }

    /**
     * Overrides the trace method from the superclass.
     * This method logs a trace message with a marker, using a format string and one argument.
     * The method first formats the message using the given format and argument using the MessageFormatter class.
     * Then it calls the trace method of the superclass, passing in the marker and the formatted message.
     *
     * @param marker the marker for the trace message
     * @param format the format string to be used for formatting the message
     * @param arg the argument to be substituted in the format string
     */
    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        this.trace(marker, MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * Logs a trace level message with the specified marker and format, replacing the placeholders in the format string
     * with the given arguments.
     *
     * @param marker   the marker associated with the log message
     * @param format   the format string that defines the log message
     * @param arg1     the first argument to replace the placeholder in the format string
     * @param arg2     the second argument to replace the placeholder in the format string
     */
    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.trace(marker, MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * Logs a trace level message with the specified marker and format.
     * The format can contain placeholders for arguments that will be replaced
     * during the formatting process.
     * The trace level message is then logged using the formatted message.
     *
     * @param marker the marker associated with the trace message
     * @param format the format string for the trace message
     * @param arguments the arguments to be replaced in the format string
     */
    @Override
    public void trace(final Marker marker, final String format, final Object... arguments) {
        this.trace(marker, MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Logs a trace message with the specified marker, message, and throwable.
     *
     * @param marker the marker associated with the log message (nullable)
     * @param msg the log message
     * @param t the throwable to be logged (nullable)
     */
    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        this.logger.trace(marker, msg, this.labels.get(), kv("exception", t));
    }

    /**
     * Returns a boolean value indicating whether the debug mode is enabled.
     * This method checks if the logger associated with the current instance is in debug mode.
     *
     * @return true if debug mode is enabled, false otherwise
     */
    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    /**
     * Logs a debug message with the given message.
     *
     * @param msg the debug message to be logged
     */
    @Override
    public void debug(final String msg) {
        this.logger.debug(msg, this.labels.get());
    }

    /**
     * Logs a debug message with a single argument.
     *
     * This method takes a formatting string, along with an argument, and formats them using the MessageFormatter class.
     * The formatted message is then logged as a debug message.
     *
     * @param format   the formatting string for the message
     * @param arg      the argument to be included in the message
     */
    @Override
    public void debug(final String format, final Object arg) {
        this.debug(MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * Logs a debug message with two arguments.
     * This method formats the arguments using the specified format and passes them to the debug method
     * that takes a single argument.
     *
     * @param format the format string for the debug message
     * @param arg1 the first argument to be formatted and logged
     * @param arg2 the second argument to be formatted and logged
     */
    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        this.debug(MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * Formats a debug message using the given format string and arguments, and then logs it.
     *
     * @param format the format string for the debug message
     * @param arguments the arguments to be inserted into the format string
     */
    @Override
    public void debug(final String format, final Object... arguments) {
        this.debug(MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Prints a debug message with an optional throwable.
     * This method calls the debug method of the logger and passes
     * the message, labels, and the throwable.
     *
     * @param msg the message to be printed
     * @param t the throwable to be printed along with the message
     */
    @Override
    public void debug(final String msg, final Throwable t) {
        this.logger.debug(msg, this.labels.get(), kv("exception", t));
    }

    /**
     * Returns whether debug logging is enabled for the specified marker.
     *
     * @param marker the marker to check if debug logging is enabled for
     * @return true if debug logging is enabled for the specified marker, false otherwise
     */
    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return this.logger.isDebugEnabled(marker);
    }

    /**
     * Logs a debug message with a marker.
     *
     * This method logs a debug message with a marker using the underlying logger.
     * The debug message is obtained from the specified 'msg' parameter. The marker
     * is specified by the 'marker' parameter. The labels associated with the logger
     * instance are also included in the debug message.
     *
     * Parameters:
     * - marker: the marker to attach to the debug message (not null)
     * - msg: the debug message to log (not null)
     *
     * Return:
     * - void
     */
    @Override
    public void debug(final Marker marker, final String msg) {
        this.logger.debug(marker, msg, this.labels.get());
    }

    /**
     * Logs a debug message with a single argument using a specified marker.
     * The message is formatted using the format string and the argument provided.
     *
     * @param marker the marker to associate with the debug message
     * @param format the format string for the debug message
     * @param arg the argument to be formatted and included in the debug message
     * (no return value)
     */
    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        this.debug(marker, MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * This method is used to log debug messages with a specified marker, format, arg1, and arg2.
     * It calls the debug method with the marker and the formatted message as arguments.
     *
     * @param marker the marker associated with the debug message
     * @param format the format of the debug message
     * @param arg1 the first argument to be formatted in the debug message
     * @param arg2 the second argument to be formatted in the debug message
     */
    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.debug(marker, MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * This method is used to log debug messages with a specified marker and format.
     * It takes in a marker, a format string, and a variable number of arguments.
     * The format string is used to format the message with the specified arguments using MessageFormatter.
     * The formatted message is then logged as a debug message with the specified marker.
     *
     * @param marker - the marker for the log message
     * @param format - the format string for the message
     * @param arguments - the arguments to be formatted into the message
     */
    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {
        this.debug(marker, MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Debugs a log message with an optional marker and throwable.
     *
     * @param marker - the marker to associate with the log message (optional)
     * @param msg - the log message to be debugged
     * @param t - the throwable to be included in the log message (optional)
     */
    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        this.logger.debug(marker, msg, this.labels.get(), kv("exception", t));
    }

    /**
     * Checks whether the logger level for 'info' messages is enabled.
     *
     * @return true if the logger level for 'info' messages is enabled, false otherwise.
     */
    @Override
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    /**
     * Logs an info message with the given message.
     *
     * @param msg the message to log
     */
    @Override
    public void info(final String msg) {
        this.logger.info(msg, this.labels.get());
    }

    /**
     * Logs an informational message with a single argument.
     *
     * This method takes a message format and an argument, formats
     * the message using the provided format and argument, and then
     * logs the resulting message as an informational message.
     *
     * @param format the message format string
     * @param arg the argument to be substituted in the message format
     */
    @Override
    public void info(final String format, final Object arg) {
        this.info(MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * Logs a message at the INFO level with two arguments.
     *
     * @param format the message format string
     * @param arg1 the first argument
     * @param arg2 the second argument
     */
    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        this.info(MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * Logs an info message with a specific format and arguments.
     *
     * This method takes a format string and variable number of arguments to create an info message,
     * using the MessageFormatter.format() method from the MessageFormatter class. The resulting message
     * is then passed to the info() method of the current object to be logged.
     *
     * @param format the format string for the info message
     * @param arguments the variable number of arguments to be formatted into the message
     */
    @Override
    public void info(final String format, final Object... arguments) {
        this.info(MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Logs an informational message with an optional throwable.
     * This method appends the given message, the current labels, and the exception information to the logger, using the info level.
     *
     * @param msg The message to be logged.
     * @param t Optional Throwable object representing an exception to be logged..
     */
    @Override
    public void info(final String msg, final Throwable t) {
        this.logger.info(msg, this.labels.get(), kv("exception", t));
    }

    /**
     * Checks if the given marker is enabled for the INFO level. This is a wrapper method
     * that delegates the call to the underlying logger's isInfoEnabled() method with the provided marker.
     *
     * @param marker the marker to check if enabled for INFO level
     * @return true if the marker is enabled for INFO level, false otherwise
     */
    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return this.logger.isInfoEnabled(marker);
    }

    /**
    * Logs an information message with the specified marker and message.
    *
    * @param marker the marker object to associate with the log message
    * @param msg the information message to be logged
    *
    * Parameters:
    * - marker (Marker): the marker object to associate with the log message
    * - msg (String): the information message to be logged
    *
    * Return value: void
    */
    @Override
    public void info(final Marker marker, final String msg) {
        this.logger.info(marker, msg, this.labels.get());
    }

    /**
     * Logs an INFO level message with the specified marker, format, and argument.
     *
     * This method formats the message using the specified format and argument using the MessageFormatter class.
     * Then it calls the info method with the generated message.
     *
     * @param marker the marker to be associated with the log message
     * @param format the format of the log message
     * @param arg the argument to be included in the log message
     */
    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        this.info(marker, MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * Logs a message at the INFO level with the specified marker, format, and two arguments.
     * Calls the {@code info} method with the transformed message obtained by formatting the specified format and arguments using the MessageFormatter class.
     *
     * @param marker the marker to associate with the log message
     * @param format the format string for the log message
     * @param arg1 the first argument to be formatted in the log message
     * @param arg2 the second argument to be formatted in the log message
     */
    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.info(marker, MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * Logs an informational message with the given marker and format. This method uses MessageFormatter to format the
     * message with the provided arguments and then calls the info method with the marker and formatted message.
     *
     * @param marker     the marker associated with the log message
     * @param format     the format string for the log message
     * @param arguments  the arguments to be formatted with the format string
     */
    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
        this.info(marker, MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Sends an info log message with the specified marker, message, and exception.
     *
     * @param marker The marker associated with the log message.
     * @param msg The log message to be sent.
     * @param t The exception associated with the log message.
     */
    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        this.logger.info(marker, msg, this.labels.get(), kv("exception", t));
    }

    /**
     * Checks if the warn level is enabled for logging.
     *
     * @return {@code true} if the warn level is enabled,
     *         {@code false} otherwise.
     */
    @Override
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    /**
     * Logs a warning message using the logger instance.
     * The message is formatted with the provided arguments and the logger level is set to WARNING.
     *
     * @param msg the warning message to be logged
     */
    @Override
    public void warn(final String msg) {
        this.logger.warn(msg, this.labels.get());
    }

    /**
     * Generates a warning message using the provided format and argument, and
     * logs it to the appropriate destination.
     *
     * @param format the format string specifying the content of the warning message
     * @param arg the object argument to be formatted and included in the message
     */
    @Override
    public void warn(final String format, final Object arg) {
        this.warn(MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * Sends a warning message with the given format and arguments.
     *
     * @param format the format string for the warning message
     * @param arg1 the first argument to be included in the warning message
     * @param arg2 the second argument to be included in the warning message
     */
    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        this.warn(MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * Logs a warning message with the specified format and arguments.
     * <p>
     * This method formats the warning message by using the {@link MessageFormatter} class. It takes the
     * provided format string and arguments, substitutes the arguments into the string, and retrieves the
     * formatted message. The resulting formatted message is then logged as a warning using the {@link Logger}.
     * </p>
     *
     * @param format the format string for the warning message
     * @param arguments the arguments to be substituted into the format string
     *
     * @throws IllegalArgumentException if the format string is null
     */
    @Override
    public void warn(final String format, final Object... arguments) {
        this.warn(MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Logs a warning message with an optional exception.
     *
     * This method logs a warning message using the logger component, along
     * with an optional exception if provided. The warning message should
     * provide information about the occurrence of an unexpected or potentially
     * problematic situation. The optional exception parameter can be used to
     * provide additional context or stack trace information.
     *
     * @param msg the warning message to be logged
     * @param t the optional exception to be logged
     */
    @Override
    public void warn(final String msg, final Throwable t) {
        this.logger.warn(msg,
                this.labels.get(), kv("exception", t));
    }

    /**
     * Checks if the warn level is enabled for this logger instance. The method
     * delegates the check to the underlying logger instance by calling the
     * logger.isWarnEnabled() method with the specified marker.
     *
     * @param marker the marker to be checked
     * @return true if the warn level is enabled for this logger instance, false otherwise
     */
    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return this.logger.isWarnEnabled(marker);
    }

    /**
     * Writes a warning message to the logger with a specific marker and a message.
     * The warning message is written by invoking the warn method of the logger object,
     * passing the marker, message, and the labels as arguments.
     *
     * @param marker the marker associated with the log message
     * @param msg the warning message to be logged
     */
    @Override
    public void warn(final Marker marker, final String msg) {
        this.logger.warn(marker, msg, this.labels.get());
    }

    /**
     * Logs a warning message with a specific marker.
     * The message is generated by formatting the given format string using the given argument.
     * This method then delegates to the warn method to log the actual message.
     *
     * @param marker the marker associated with the warning message
     * @param format the format string used to generate the warning message
     * @param arg the argument used to format the warning message
     */
    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        this.warn(marker, MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * Generates a log message at the WARNING level with a marker, a format and two arguments.
     * The method formats the log message using the given format and arguments,
     * then calls the warn method with the marker and the formatted message.
     *
     * @param marker the marker associated with the log message
     * @param format the format string for the log message
     * @param arg1 the first argument to be formatted in the log message
     * @param arg2 the second argument to be formatted in the log message
     */
    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.warn(marker, MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * Logs a warning message with the specified marker, format string, and arguments.
     *
     * This method formats the message using the specified format string and arguments
     * and then delegates the logging to the 'warn' method that logs the formatted
     * message with the specified marker.
     *
     * @param marker The marker associated with the warning.
     * @param format The format string for the warning message.
     * @param arguments The arguments to be used in the format string.
     */
    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
        this.warn(marker, MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Logs a warning message with an optional marker and an exception.
     *
     * @param marker   The marker object associated with the log message (optional).
     * @param msg      The warning message to be logged.
     * @param t        The exception to be logged.
     */
    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        this.logger.warn(marker, msg, this.labels.get(), kv("exception", t));
    }

    /**
     * Returns a boolean value indicating whether error level logging is enabled for this logger.
     *
     * @return true if error level logging is enabled, false otherwise
     */
    @Override
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    /**
     * Logs an error message using the provided message and labels.
     *
     * @param msg The error message to be logged.
     */
    @Override
    public void error(final String msg) {
        this.logger.error(msg, this.labels.get());
    }

    /**
     * Logs an error message with a formatted string and an argument.
     * The method takes a format string and an argument, formats the message using MessageFormatter,
     * and then logs the formatted message as an error.
     *
     * @param format the string format for the error message
     * @param arg the argument to be inserted into the format string
     */
    @Override
    public void error(final String format, final Object arg) {
        this.error(MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * Formats a message and passes it to the error method.
     *
     * This method takes a format string and two arguments, and uses the MessageFormatter.format() method to format the message using the given format and arguments. The formatted message is then passed to the error method.
     *
     * @param format the format string to be used for message formatting
     * @param arg1 the first argument to be included in the formatted message
     * @param arg2 the second argument to be included in the formatted message
     */
    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        this.error(MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * Logs an error message with placeholders and arguments.
     * This method formats the given error message using the provided format string and arguments,
     * and then logs the resulting message as an error.
     *
     * @param format - the format string for the error message, containing placeholders for
     *        the arguments in the form of "{}"
     * @param arguments - the objects to be substituted in the placeholders of the format string
     */
    @Override
    public void error(final String format, final Object... arguments) {
        this.error(MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Logs an error message with an associated throwable.
     *
     * This method logs an error message along with an optional throwable. The error message
     * is provided as a parameter 'msg'. The associated throwable (if any) is provided as a
     * parameter 't'. The error is logged using the logger's 'error' method, with the error
     * message as the first argument. The logger's labels are also included in the log message,
     * obtained from the 'labels' field of the class. If a throwable is provided, it is wrapped
     * in a key-value pair with the key 'exception' and included in the log message using the
     * logger's 'error' method.
     *
     * @param msg          the error message to be logged
     * @param t            the throwable associated with the error (may be null)
     */
    @Override
    public void error(final String msg, final Throwable t) {
        this.logger.error(msg,
                this.labels.get(), kv("exception", t));
    }

    /**
     * Checks if the error level is enabled for a given marker.
     *
     * @param marker the marker to be checked
     * @return true if the error level is enabled for the marker, false otherwise
     */
    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return this.logger.isErrorEnabled(marker);
    }

    /**
     * Overrides the error method of the logger to log an error message with a specific marker and message.
     *
     * @param marker the marker to associate with the error message
     * @param msg the error message to be logged
     */
    @Override
    public void error(final Marker marker, final String msg) {
        this.logger.error(marker, msg, this.labels.get());
    }

    /**
     * Logs an error message with the specified format and argument,
     * using the provided marker as a reference.
     *
     * @param marker the marker used as a reference for the error
     * @param format the format of the error message
     * @param arg the argument to be included in the error message
     * @throws IllegalArgumentException if the format is invalid
     */
    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        this.error(marker, MessageFormatter.format(format, arg).getMessage());
    }

    /**
     * Logs an error message with the specified marker, format string, and arguments.
     * The format string can contain placeholders that will be replaced by the provided arguments.
     * The formatted message will be logged as an error.
     *
     * @param marker   the marker associated with the error message
     * @param format   the format string for the error message
     * @param arg1     the first argument to be replaced in the format string
     * @param arg2     the second argument to be replaced in the format string
     */
    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        this.error(marker, MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    /**
     * Logs an error message with a marker, using the given format and arguments.
     * The format string can contain placeholder "{}" that will be replaced by the values in the arguments array.
     * The formatted message will be passed to a logger with an error level.
     *
     * @param marker    the marker associated with the error message
     * @param format    the format string of the error message
     * @param arguments the arguments to fill in the placeholders in the format string
     */
    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {
        this.error(marker, MessageFormatter.format(format, arguments).getMessage());
    }

    /**
     * Logs an error message with an associated marker and optional throwable.
     *
     * @param marker the marker associated with the error message
     * @param msg the error message to be logged
     * @param t the optional throwable to be logged
     */
    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        this.logger.error(marker, msg, this.labels.get(), kv("exception", t));
    }
}
