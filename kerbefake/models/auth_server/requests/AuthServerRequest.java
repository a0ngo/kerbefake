package kerbefake.models.auth_server.requests;

import kerbefake.errors.InvalidRequestException;
import kerbefake.models.auth_server.AuthServerRequestHeader;

/**
 * An abstract class representing a request to the authentication server
 */
public abstract class AuthServerRequest<B extends AuthServerRequestBody> {

    private AuthServerRequestHeader header;
    private B body;

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
    public abstract void execute();

    public static <B extends AuthServerRequestBody> AuthServerRequest<? extends AuthServerRequestBody> buildFor(AuthServerRequestHeader header, B body) throws InvalidRequestException {
        switch(header.getCode()){
            case REGISTER_CLIENT:
                return new RegisterClientRequest(header, (RegisterClientRequestBody) body);
            default:
                throw new InvalidRequestException("Code");
        }
    }

}
