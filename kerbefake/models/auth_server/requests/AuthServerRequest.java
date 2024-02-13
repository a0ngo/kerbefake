package kerbefake.models.auth_server.requests;

import kerbefake.errors.InvalidRequestException;
import kerbefake.errors.RequestExecutionException;
import kerbefake.models.auth_server.AuthServerRequestHeader;

/**
 * An abstract class representing a request to the authentication server
 */
public abstract class AuthServerRequest<B extends AuthServerRequestBody, E> {

    protected AuthServerRequestHeader header;
    protected B body;

    public AuthServerRequest(AuthServerRequestHeader header, B body){
        this.header = header;
        this.body = body;
    }

    public AuthServerRequestHeader getHeader() {
        return header;
    }

    public B getBody() {
        return body;
    }

    /**
     * Executes the request that was provided
     */
    public abstract E execute() throws RequestExecutionException;

    public static <B extends AuthServerRequestBody> AuthServerRequest<? extends AuthServerRequestBody, ?> buildFor(AuthServerRequestHeader header, B body) throws InvalidRequestException {
        switch(header.getCode()){
            case REGISTER_CLIENT:
                return new RegisterClientRequest(header, (RegisterClientRequestBody) body);
            default:
                throw new InvalidRequestException("Code");
        }
    }

}
