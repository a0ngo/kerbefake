package kerbefake.common.errors;

public class InvalidHexStringException extends Exception{

    public InvalidHexStringException(String hex, int location){
        super(String.format("Invalid hex character in position %d string is: %s", location, hex));
    }
}
