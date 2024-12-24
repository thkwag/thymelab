package com.github.thkwag.thymelab.launcher.util;

import com.github.thkwag.thymelab.launcher.process.LogConsumer;

public class AppLogger {
    private static LogConsumer logConsumer;
    private static final String ERROR_PREFIX = "[ERROR] ";
    private static final String INFO_PREFIX = "[INFO] ";
    private static final String DEBUG_PREFIX = "[DEBUG] ";
    private static final String WARN_PREFIX = "[WARN] ";
    private static final String NEW_LINE = "\n";
    private static LogLevel currentLevel = LogLevel.INFO;

    public enum LogLevel {
        ERROR(0),
        WARN(1),
        INFO(2),
        DEBUG(3);

        private final int priority;

        LogLevel(int priority) {
            this.priority = priority;
        }

        public boolean isLoggable(LogLevel level) {
            return this.priority >= level.priority;
        }

        public static LogLevel fromString(String level) {
            try {
                return valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                return INFO;  // Default to INFO if invalid
            }
        }
    }

    public static void setLogConsumer(LogConsumer consumer) {
        logConsumer = consumer;
    }

    public static void setLogLevel(String level) {
        currentLevel = LogLevel.fromString(level);
    }

    public static void info(String message) {
        if (logConsumer != null && currentLevel.isLoggable(LogLevel.INFO)) {
            logConsumer.accept(INFO_PREFIX + message + NEW_LINE);
        }
    }

    public static void error(String message) {
        if (logConsumer != null && currentLevel.isLoggable(LogLevel.ERROR)) {
            logConsumer.accept(ERROR_PREFIX + message + NEW_LINE);
        }
    }

    public static void error(String message, Throwable throwable) {
        if (logConsumer != null && currentLevel.isLoggable(LogLevel.ERROR)) {
            StringBuilder sb = new StringBuilder();
            sb.append(ERROR_PREFIX).append(message);
            if (throwable != null) {
                sb.append(NEW_LINE).append(throwable.getMessage());
                for (StackTraceElement element : throwable.getStackTrace()) {
                    sb.append(NEW_LINE).append("    at ").append(element.toString());
                }
            }
            sb.append(NEW_LINE);
            logConsumer.accept(sb.toString());
        }
    }

    public static void warn(String message) {
        if (logConsumer != null && currentLevel.isLoggable(LogLevel.WARN)) {
            logConsumer.accept(WARN_PREFIX + message + NEW_LINE);
        }
    }

    public static void debug(String message) {
        if (logConsumer != null && currentLevel.isLoggable(LogLevel.DEBUG)) {
            logConsumer.accept(DEBUG_PREFIX + message + NEW_LINE);
        }
    }
} 