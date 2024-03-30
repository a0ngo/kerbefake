package kerbefake.common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A class with logging functionality, for neat logging.
 */
public final class Logger {

    private static final Map<LoggerType, Logger> loggers = new HashMap<>();

    public static Logger getLogger(LoggerType type) {
        return getLogger(type, true, true, LogLevel.DEBUG, LogLevel.INFO);
    }

    public static Logger getLogger(LoggerType type, boolean logToFile, boolean logToConsole) {
        return getLogger(type, logToFile, logToConsole, LogLevel.DEBUG, LogLevel.INFO);
    }

    public static Logger getLogger(LoggerType type, LogLevel minimalLevelToFile, LogLevel minimalLevelToConsole) {
        return getLogger(type, true, true, minimalLevelToFile, minimalLevelToConsole);
    }

    public static Logger getLogger(LoggerType type, boolean logToFile, boolean logToConsole, LogLevel minimalLevelToFile, LogLevel minimalLevelToConsole) {
        return loggers.computeIfAbsent(type, t -> {
            try {
                return new Logger(t, logToFile, logToConsole, minimalLevelToFile, minimalLevelToConsole);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private final LoggerType type;

    private final boolean logToFile;

    private final boolean logToConsole;

    private final LogLevel minimalLevelToConsole;

    private final LogLevel minimalLevelToFile;

    private FileWriter logWriter = null;

    public static final Logger commonLogger = getLogger(LoggerType.COMMON_LOGGER);

    private Logger(LoggerType type, boolean logToFile, boolean logToConsole, LogLevel levelForFile, LogLevel levelForConsole) throws IOException {
        this.type = type;
        this.logToFile = logToFile;
        this.logToConsole = logToConsole;
        this.minimalLevelToFile = levelForFile;
        this.minimalLevelToConsole = levelForConsole;
        if (logToFile) {
            logWriter = new FileWriter(type.getFileName());
        }
    }

    public enum LoggerType {
        AUTH_SERVER_LOGGER("[AUTH  ]", "auth_server.log"),
        MESSAGE_SERVER_LOGGER("[MSG   ]", "msg_server.log"),
        CLIENT_LOGGER("[CLIENT]", "client_server.log"),
        TEST_LOGGER("[TEST  ]", "test.log"),
        COMMON_LOGGER("[COMMON]", "common.log");

        private final String logPrefix;
        private final String fileName;

        LoggerType(String logPrefix, String fileName) {
            this.logPrefix = logPrefix;
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        public String getLogPrefix() {
            return logPrefix;
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

    public void print(String message, Object... args) {
        System.out.printf("%s%n", String.format(message, args));
    }

    public void info(String message, Object... args) {
        log(LogLevel.INFO, message, args);
    }

    public void infoToFileOnly(String message, Object... args) {
        log(LogLevel.INFO, true, message, args);
    }

    public void error(String message, Object... args) {
        log(LogLevel.ERROR, message, args);
    }

    public void errorToFileOnly(String message, Object... args) {
        log(LogLevel.ERROR, true, message, args);
    }

    public void error(Throwable e) {
        StringBuilder stackBuilder = new StringBuilder();
        for (StackTraceElement elem : e.getStackTrace()) {
            stackBuilder.append(elem.toString());
        }
        log(LogLevel.ERROR, true, e.getMessage(), "\n", stackBuilder.toString());
    }

    public void warn(String message, Object... args) {
        log(LogLevel.WARN, message, args);
    }

    public void warnToFileOnly(String message, Object... args) {
        log(LogLevel.WARN, true, message, args);
    }

    public void debug(String message, Object... args) {
        log(LogLevel.DEBUG, message, args);
    }

    public void debugToFileOnly(String message, Object... args) {
        log(LogLevel.DEBUG, true, message, args);
    }


    private void log(LogLevel logLevel, String message, Object... args) {
        log(logLevel, false, message, args);
    }

    private void log(LogLevel logLevel, boolean fileOnly, String message, Object... args) {
        String fullMessage = String.format("%7s%7s:%s%n", type.logPrefix, logLevel.getLevel(), String.format(message, args));
        if (logToConsole)
            if (logLevel.ordinal() >= minimalLevelToConsole.ordinal())
                System.out.printf(fullMessage);
        if (logToFile) {
            if (fileOnly || logLevel.ordinal() >= minimalLevelToFile.ordinal()) {
                try {
                    logWriter.write(fullMessage);
                    logWriter.flush();
                } catch (IOException e) {
                    logWriter = null;
                    error("Failed to write to log file, stopping any further attempts to write to file, failed due to: %s", e.getMessage());
                }
            }
        }
    }
}
