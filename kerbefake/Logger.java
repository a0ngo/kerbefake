package kerbefake;

/**
 * A class with logging functionality, for neat logging.
 */
public final class Logger {

    public static void print(String message, Object... args) {
        System.out.printf("%s%n", String.format(message, args));
    }

    public static void info(String message, Object... args) {
        log("[INFO ]", message, args);
    }

    public static void error(String message, Object... args) {
        log("[ERROR]", message, args);
    }

    public static void warn(String message, Object... args) {
        log("[WARN ]", message, args);
    }

    public static void debug(String message, Object... args) {
        log("[DEBUG]", message, args);
    }

    private static void log(String level, String message, Object... args) {
        System.out.printf("%7s %s%n", level, String.format(message, args));
    }
}
