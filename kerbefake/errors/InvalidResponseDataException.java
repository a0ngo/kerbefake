package kerbefake.errors;

public class InvalidResponseDataException extends Exception {

    public InvalidResponseDataException(String field) {
        super(String.format("Response creation failed as field %s is invalid.", field));
    }
}
