package kerbefake.errors;

/**
 * An exception that indicates that the client configuration file is invalid or missing.
 */
public class InvalidClientConfigException extends Exception {

    public InvalidClientConfigException() {
        super("The file me.info is missing or can't be found.");
    }

    public InvalidClientConfigException(String message) {
        super(message);
    }


}
