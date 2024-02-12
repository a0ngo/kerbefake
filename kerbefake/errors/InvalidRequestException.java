package kerbefake.errors;

public class InvalidRequestException extends Exception {

    public InvalidRequestException(String field) {
        super(String.format("Request is invalid, field %s is invalid and is not a valid value", field));
    }
}
