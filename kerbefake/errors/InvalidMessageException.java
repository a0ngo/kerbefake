package kerbefake.errors;

import kerbefake.models.MessageCode;

public class InvalidMessageException extends Exception{

    public InvalidMessageException(MessageCode code){
        super(String.format("Unknown message code: %d", code.getCode()));
    }

    public InvalidMessageException(String message){
        super(message);
    }

    public InvalidMessageException(){
        super("Unable to decode header bytestream.");
    }
}
