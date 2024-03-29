package kerbefake.common.errors;

public class InvalidMessageCodeException extends Exception {

    public InvalidMessageCodeException(String field) {
        super(String.format("Request is invalid, field %s is invalid", field));
    }
}
