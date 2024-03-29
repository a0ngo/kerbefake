package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.errors.InvalidHexStringException;
import kerbefake.errors.InvalidMessageException;
import kerbefake.models.MessageCode;
import kerbefake.models.ServerMessage;
import kerbefake.models.ServerMessageHeader;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequest;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequestBody;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponseBody;

import java.io.IOException;

import static kerbefake.Constants.ClientConstants.REQUEST_FAILED;
import static kerbefake.Constants.ID_LENGTH;
import static kerbefake.Constants.ResponseCodes.REGISTER_CLIENT_FAILURE_CODE;
import static kerbefake.Constants.ResponseCodes.UNKNOWN_FAILURE_CODE;
import static kerbefake.Constants.SERVER_VERSION;
import static kerbefake.Logger.error;
import static kerbefake.client.UserInputOutputHandler.getNameFromUser;

/**
 * This class
 */
public final class RegisterOperation extends ClientOperation<RegisterClientRequest, String> {

    private char[] plaintextPassword;

    public RegisterOperation(ClientConnection connection, char[] plaintextPassword) {
        super(connection);
        this.plaintextPassword = plaintextPassword;
    }

    @Override
    public String perform() {
        String name = getNameFromUser();

        RegisterClientRequestBody registerClientRequestBody = new RegisterClientRequestBody(name, this.plaintextPassword);
        ServerMessageHeader registerClentHeader = new ServerMessageHeader(SERVER_VERSION, MessageCode.REGISTER_CLIENT, registerClientRequestBody.toLEByteArray().length);
        RegisterClientRequest registerClientRequest = new RegisterClientRequest(registerClentHeader, registerClientRequestBody);

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
