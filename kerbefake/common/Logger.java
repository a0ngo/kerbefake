package kerbefake.common;

import java.io.FileWriter;
import java.io.IOException;

/**
 * A class with logging functionality, for neat logging.
 */
public final class Logger {

    public enum LoggerType {
        AUTH_SERVER_LOGGER("auth_server.log"),
        MESSAGE_SERVER_LOGGER("msg_server.log"),
        CLIENT_LOGGER("client_server.log");

        private final String fileName;

        LoggerType(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public enum LogLevel {
        DEBUG("[DEBUG]"),
        INFO("[INFO ]"),
        WARN("[WARN ]"),
        ERROR("[ERROR]");
        private final String level;

        LogLevel(String level) {
            this.level = level;
        }

        public String getLevel() {
            return level;
        }

    }

    private static int MINIMAL_LOG_LEVEL_TO_LOG_TO_FILE;

    private static FileWriter logWritter = null;

    public static void initializeFileLogger(LoggerType logger, LogLevel minimalLevelToLogToFile) throws IOException {
        if (logWritter != null) {
            throw new RuntimeException("Tried to initialize logger for file but it was already initialized");
        }

        logWritter = new FileWriter(logger.fileName);
        MINIMAL_LOG_LEVEL_TO_LOG_TO_FILE = minimalLevelToLogToFile.ordinal();
    }

    public static void print(String message, Object... args) {
        System.out.printf("%s%n", String.format(message, args));
    }

    public static void info(String message, Object... args) {
        log(LogLevel.INFO, message, args);
    }

    public static void infoToFileOnly(String message, Object... args) {
        log(LogLevel.INFO, true, message, args);
    }

    public static void error(String message, Object... args) {
        log(LogLevel.ERROR, message, args);
    }

    public static void errorToFileOnly(String message, Object... args) {
        log(LogLevel.ERROR, true, message, args);
    }

    public static void error(Throwable e) {
        StringBuilder stackBuilder = new StringBuilder();
        for (StackTraceElement elem : e.getStackTrace()) {
            stackBuilder.append(elem.toString());
        }
        log(LogLevel.ERROR, true, e.getMessage(), "\n", stackBuilder.toString());
    }

    public static void warn(String message, Object... args) {
        log(LogLevel.WARN, message, args);
    }

    public static void warnToFileOnly(String message, Object... args) {
        log(LogLevel.WARN, true, message, args);
    }

    public static void debug(String message, Object... args) {
        log(LogLevel.DEBUG, message, args);
    }

    public static void debugToFileOnly(String message, Object... args) {
        log(LogLevel.DEBUG, true, message, args);
    }


    private static void log(LogLevel logLevel, String message, Object... args) {
        log(logLevel, false, message, args);
    }

    private static void log(LogLevel logLevel, boolean fileOnly, String message, Object... args) {
        String fullMessage = String.format("%7s %s%n", logLevel.getLevel(), String.format(message, args));
        if (!fileOnly)
            System.out.printf(fullMessage);
        if (logWritter != null && logLevel.ordinal() >= MINIMAL_LOG_LEVEL_TO_LOG_TO_FILE) {
            try {
                logWritter.write(fullMessage);
            } catch (IOException e) {
                logWritter = null;
                error("Failed to write to log file, stopping any further attempts to write to file, failed due to: %s", e.getMessage());
            }
        }
    }
}
