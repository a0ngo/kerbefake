package kerbefake.models.auth_server.requests;

import kerbefake.errors.InvalidRequestException;
import kerbefake.models.auth_server.RequestCode;

/**
 * The basic class that defines the basic request body functionality.
 */
public abstract class AuthServerRequestBody {

    /**
     * Gets a body as raw bytes
     * @return a byte array in little endian representing the request
     */
    public abstract byte[] getRawBody();

    /**
     * Parses a given set of bytes according to the fields of the object.
     * @param bytes - the bytes to parse
     * @throws InvalidRequestException - in case of a validation error
     */
    protected abstract AuthServerRequestBody parse(byte[] bytes) throws InvalidRequestException;


    /**
     * Converts a byte array to the corresponding request object
     * @param bodyBytes - the body bytes
     * @return A class extending {@link AuthServerRequestBody}
     * @throws InvalidRequestException - In case of a validation error
     */
    public static AuthServerRequestBody parse(byte[] bodyBytes, RequestCode code) throws InvalidRequestException {
        if(bodyBytes == null){
            throw new InvalidRequestException("Body size");
        }
        if(code == null){
            throw new InvalidRequestException("Request code");
        }

        switch(code){
            case REGISTER_CLIENT:
                return new RegisterClientRequestBody().parse(bodyBytes);

            default:
                throw new InvalidRequestException("Code");
        }
    }
}
