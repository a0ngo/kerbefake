package kerbefake.msg_server.errors;

public class InvalidMessageServerDataException extends Exception {

    public InvalidMessageServerDataException(String component) {
        super(String.format("Invalid data in msg.info file, specifically the %s is invalid", component));
    }

}
