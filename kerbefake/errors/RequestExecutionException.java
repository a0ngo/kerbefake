package kerbefake.errors;

public class RequestExecutionException extends Exception{

    public RequestExecutionException(String clazzName, String message){
        super(String.format("Failed to execute request of type %s: %s", clazzName, message));
    }
}
