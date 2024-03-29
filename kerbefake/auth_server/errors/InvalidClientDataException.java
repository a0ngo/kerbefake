package kerbefake.auth_server.errors;

public class InvalidClientDataException extends Exception {

    public InvalidClientDataException(String fieldId) {
        super(String.format("Client data provided is invalid, field %s does not match constraints", fieldId));
    }
}