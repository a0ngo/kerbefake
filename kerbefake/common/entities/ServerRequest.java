package kerbefake.common.entities;

import kerbefake.common.errors.InvalidHexStringException;

/**
 * An abstract class representing a request to the authentication server
 */
public interface ServerRequest {

    /**
     * Executes this request and returns the response (in case of a failure returns {@link kerbefake.auth_server.entities.responses.FailureResponse}.
     *
     * @return a response (in the form of an AuthServerMessage) or {@link kerbefake.auth_server.entities.responses.FailureResponse} in case of a failure.
     */
    ServerMessage execute() throws InvalidHexStringException;
}