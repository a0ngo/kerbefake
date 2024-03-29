package kerbefake.models;

/**
 * An abstract class representing a request to the authentication server
 */
public interface ServerRequest {

    /**
     * Executes this request and returns the response (in case of a failure returns {@link kerbefake.models.auth_server.responses.FailureResponse}.
     *
     * @return a response (in the form of an AuthServerMessage) or {@link kerbefake.models.auth_server.responses.FailureResponse} in case of a failure.
     */
    ServerMessage execute();
}