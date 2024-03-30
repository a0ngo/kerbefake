package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.common.errors.InvalidHexStringException;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequest;
import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequestBody;
import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponse;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponseBody;

import java.io.IOException;

import static kerbefake.common.Constants.ClientConstants.REQUEST_FAILED;
import static kerbefake.common.Constants.ID_LENGTH;
import static kerbefake.common.Constants.ResponseCodes.REGISTER_CLIENT_FAILURE_CODE;
import static kerbefake.common.Constants.ResponseCodes.UNKNOWN_FAILURE_CODE;
import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.Logger.error;
import static kerbefake.client.UserInputOutputHandler.getNameFromUser;

/**
 * This class
 */
public final class RegisterOperation extends ClientOperation<RegisterClientRequest, String> {

    private char[] plaintextPassword;
    private final String clientID;

    public RegisterOperation(ClientConnection connection, char[] plaintextPassword, String clientID) {
        super(connection);
        this.plaintextPassword = plaintextPassword;
        this.clientID = clientID;
    }

    @Override
    public String perform() {
        String name = getNameFromUser();

        RegisterClientRequestBody registerClientRequestBody = new RegisterClientRequestBody(name, this.plaintextPassword);
        ServerMessageHeader registerClientHeader = new ServerMessageHeader(this.clientID, SERVER_VERSION, MessageCode.REGISTER_CLIENT,
            registerClientRequestBody.toLEByteArray().length);
        RegisterClientRequest registerClientRequest = new RegisterClientRequest(registerClientHeader, registerClientRequestBody);

        try {
            ServerMessage response = this.internalPerform(registerClientRequest);
            if (response instanceof FailureResponse) {
                switch (response.getHeader().getMessageCode().getCode()) {
                    case UNKNOWN_FAILURE_CODE:
                        error(REQUEST_FAILED);
                        return null;
                    case REGISTER_CLIENT_FAILURE_CODE:
                        error("Failed to register client, you might already be registered, in that case please ask the admin to reset your user and delete the me.info file.");
                        return null;
                    default:
                        error("Received unknown response code: %d - can't proceed.", response.getHeader().getMessageCode().getCode());
                        return null;
                }
            }

            if (!(response instanceof RegisterClientResponse)) {
                error("Received unknown response, can't proceed, response is of type %s, printing response:\n %s", response.getClass().getCanonicalName(), response.toString());
                return null;
            }

            if (response.getBody() == null) {
                error("Received response but missing response body, can't proceed, please contact server admin.");
                return null;
            }

            RegisterClientResponseBody responseBody = (RegisterClientResponseBody) response.getBody();
            String clientId = responseBody.getId();

            if (clientId == null || clientId.length() != ID_LENGTH) {
                error("Invalid client ID received in the response, can't proceed, received: %s", clientId);
                return null;
            }

            return clientId;
        } catch (IOException | InvalidHexStringException e) {
            e.printStackTrace();
            if (e instanceof InvalidHexStringException) {
                // This can only happen if some hex string is invalid, but this is unlikely at this stage, we will print a message and fail regardless.
                error("Failed to encode or decode some part of the message sent or received from the server.");
                return null;
            }
            error("Failed to send a request to the auth server due to: %s.", e.getMessage());
        } catch (InvalidMessageException e) {
            e.printStackTrace();
            error("Failed to decode message from server due to: %s\nPlease forward this to your server admin.", e.getMessage());
        }
        return null;
    }
}
