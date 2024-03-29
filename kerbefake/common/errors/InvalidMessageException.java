package kerbefake.common.errors;

import kerbefake.common.entities.MessageCode;

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
