package kerbefake.client;

import java.util.Scanner;

import static kerbefake.Logger.*;

/**
 * A class containing static methods that are responsible for showing text to the user and getting input from the user.
 */
public final class UserInputOutputHandler {

    private static final Scanner inputHandler = new Scanner(System.in);


    /**
     * A regex for IP - https://stackoverflow.com/a/36760050
     */
    private static final String IP_REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

    /**
     * A very disgusting regex for an IP + port combination.
     */
    private static final String IP_AND_PORT_REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}:([1-9]|[1-9][0-9]|[1-9][0-9][0-9]|[1-9][0-9][0-9][0-9]|[1-5][0-9][0-9][0-9][0-9]|6[0-4][0-9][0-9][0-9]|65[0-4][0-9][0-9]|655[0-2][0-9]|6553[0-5])$";

    /**
     * Prompts the user for a string - this is non-confidential so the output will be printed on screen.
     *
     * @param message  - the message to show the user.
     * @param notEmpty - whether we accept an empty string
     * @return the string the user provided.
     */
    public static String promptString(String message, boolean notEmpty) {
        String response = (String) prompt(message, String.class);
        while (!notEmpty && (response == null || response.isEmpty())) {
            error("Please provide a non-empty string.");
            response = inputHandler.next();
        }

        return response;
    }

    /**
     * Prompts the user for an integer
     *
     * @param message    - the message to show the user.
     * @param startRange - the start range of the accepted values, -1 if there is no range (also endRange must be -1 in such a case)
     * @param endRange   - the end range of the accepted values, -1 if there is no range (also startRange must be -1 in such a case)
     * @return - the int provided by the user.
     */
    public static int promptInt(String message, int startRange, int endRange) {

        int response = (int) prompt(message, Integer.class);
        if (startRange > endRange) {
            warn("Range provided is invalid (%d - %d), returning user response", startRange, endRange);
            return response;
        }
        while ((startRange != -1 && endRange != -1) && (response < startRange || response > endRange)) {
            error("Please provide an integer between %d and %d", startRange, endRange);
            response = inputHandler.nextInt();
        }

        return response;
    }

    /**
     * Prompts the user for an input
     *
     * @param message   - the message to show the user
     * @param valueType - the value type of the return we expect, this enforces types from the scanner.
     * @return int if {@code valueType} is {@code Integer.class} otherwise a string
     */
    public static Object prompt(String message, Class<?> valueType) {
        System.out.println(message);
        System.out.print("> ");
        if (valueType.equals(Integer.class)) {
            return inputHandler.nextInt();
        } else {
            if (!valueType.equals(String.class))
                error("Unknown expected input type %s, defaulting to string", valueType.getCanonicalName());
            return inputHandler.next();
        }
    }

    /**
     * Gets the password from the user and immediately hashes it. The value is stored only if we're in the pre-register state
     * in which case we need it for the registration operation.
     */
    public static char[] getPasswordFromUser() {
        info("Please provide your password;");
        System.out.print("> ");
        return System.console().readPassword();
    }

    public static String getNameFromUser() {
        return promptString("Please provide your name;", true);
    }


    /**
     * Gets the server IP and port from the user.
     *
     * @param serverType     - the type of the server that we are connecting to, auth server or message server.
     * @param defaultAddress - the default address that will be used in case the user provides an empty string.
     * @return - the string that the user provided is such exists or the value of defaultAddress.
     */
    public static String getServerAddress(String serverType, String defaultAddress) {
        // This do-while loop is meant to properly parse a provided IP:Port address provided by the user, if none is provided the above
        // is kept as default.
        do {
            String serverString = promptString(String.format("Please provide the %s server address (leave empty for %s)", serverType, defaultAddress), false);
            if (serverString != null && !serverString.isEmpty()) {
                if (serverString.matches(IP_REGEX)) {
                    int port = promptInt("Server provided without port, please provide the port used.", 1, 65535);
                    return String.format("%s:%d", serverString, port);
                } else if (serverString.matches(IP_AND_PORT_REGEX)) {
                    return serverString;
                } else {
                    continue;
                }
            }
            break;
        } while (true);
        return defaultAddress;
    }

}