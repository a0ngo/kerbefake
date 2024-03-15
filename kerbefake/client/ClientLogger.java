package kerbefake.client;

public class ClientLogger {

    public static void info(String message) {
        System.out.println("INFO: " + message);
    }

    public static void error(String message) {
        System.err.println("ERROR: " + message);
    }

    public static void debug(String message) {
        System.out.println("DEBUG: " + message);
    }
}