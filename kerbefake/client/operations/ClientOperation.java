package kerbefake.client.operations;

import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.client.ClientConnection;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.errors.InvalidMessageException;

import java.io.IOException;

import static kerbefake.client.Client.clientLogger;
import static kerbefake.common.Constants.ClientConstants.REQUEST_FAILED;
import static kerbefake.common.Constants.ResponseCodes.REGISTER_CLIENT_FAILURE_CODE;
import static kerbefake.common.Constants.ResponseCodes.UNKNOWN_FAILURE_CODE;

/**
 * An abstract class that indicates something is an operation performed by the client.
 * This allows us to define that something is done by the client.
 *
 * @param <REQ> - the request type we send to the server
 * @param <RES> - the expected response type from the server
 * @param <RET> - the return type of the {@code perform}, for example the register will return a string to indicate the client code.
 */
public abstract class ClientOperation<REQ extends ServerMessage, RES extends ServerMessage, RET> {


    private final ClientConnection conn;

    private final Class<RES> responseClass;

    protected final String clientId;

    protected ClientOperation(ClientConnection connection, Class<RES> responseClass, String clientId) {
        this.conn = connection;
        this.responseClass = responseClass;
        this.clientId = clientId;
    }

    /**
     * This method generates the request that is needed for our request.
     *
     * @return the request needed to send to the server response from the server, null if there is some error.
     */
    protected abstract REQ generateRequest() throws InvalidMessageException, IOException;

    /**
     * Given a response from the server that is not a negative response, this method will perform whatever computation is required
     * to provide the return type to the caller
     *
     * @param response - the response from the server in the expected type.
     * @return the result form the server call in the specified return type.
     */
    protected abstract RET validateResponse(RES response);

    /**
     * Performs the operation.
     *
     * @return the result of the operation or null if failed
     */
    public RET perform() {
        try {
            REQ request = generateRequest();
            if (request == null) { // Some failure happened when we generated the request thus we got null.
                return null;
            }

            ServerMessage response;
            try {
                response = conn.send(request);
            } catch (InterruptedException e) {
                clientLogger.error(e);
                clientLogger.error("Client interrupted while sending a request, can't proceed.");
                return null;
            }
            if (response == null) { // Some failure happened when we received the response for the request thus we return null
                return null;
            }

            if (response instanceof FailureResponse) {
                short responseCode = response.getHeader().getMessageCode().getCode();
                switch (responseCode) {
                    case UNKNOWN_FAILURE_CODE:
                        clientLogger.error(REQUEST_FAILED);
                        return null;
                    case REGISTER_CLIENT_FAILURE_CODE:
                        clientLogger.error("Client registration failed on server.");
                        return null;
                    default:
                        clientLogger.error("Received unknown response code: %d - can't proceed.", response.getHeader().getMessageCode().getCode());
                        return null;
                }
            }

            RES properResponse;
            try {
                properResponse = responseClass.cast(response);
            } catch (ClassCastException e) {
                clientLogger.error("Received unknown response, can't proceed, response is of type %s, printing response:\n %s", response.getClass().getCanonicalName(), response.toString());
                return null;
            }

            return validateResponse(properResponse);
        } catch (IOException e) {
            clientLogger.error(e);
            clientLogger.error("Failed to send a request to the auth server due to: %s.", e.getMessage());
        } catch (InvalidMessageException e) {
            clientLogger.error(e);
            clientLogger.error("Failed to decode message from server due to: %s\nPlease forward this to your server admin.", e.getMessage());
        }

        return null;
    }
}
