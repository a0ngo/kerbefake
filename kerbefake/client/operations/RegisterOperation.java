package kerbefake.client.operations;

import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequest;
import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequestBody;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponse;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponseBody;
import kerbefake.client.ClientConnection;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessageHeader;

import java.util.Arrays;

import static kerbefake.client.UserInputOutputHandler.getNameFromUser;
import static kerbefake.common.Constants.ID_HEX_LENGTH_CHARS;
import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.Logger.error;

/**
 * This class
 */
public final class RegisterOperation extends ClientOperation<RegisterClientRequest, RegisterClientResponse, String> {

    private final char[] plaintextPassword;

    public RegisterOperation(ClientConnection connection, char[] plaintextPassword) {
        super(connection, RegisterClientResponse.class);
        this.plaintextPassword = plaintextPassword;
    }

    @Override
    protected RegisterClientRequest generateRequest() {
        String name = getNameFromUser();

        RegisterClientRequestBody registerClientRequestBody = new RegisterClientRequestBody(name, this.plaintextPassword);
        Arrays.fill(plaintextPassword, (char) 0);
        ServerMessageHeader registerClentHeader = new ServerMessageHeader(SERVER_VERSION, MessageCode.REGISTER_CLIENT, registerClientRequestBody.toLEByteArray().length);

        return new RegisterClientRequest(registerClentHeader, registerClientRequestBody);
    }

    @Override
    protected String validateResponse(RegisterClientResponse response) {
        if (response.getBody() == null) {
            error("Received response but missing response body, can't proceed, please contact server admin.");
            return null;
        }

        RegisterClientResponseBody responseBody = (RegisterClientResponseBody) response.getBody();
        String clientId = responseBody.getId();

        if (clientId == null || clientId.length() != ID_HEX_LENGTH_CHARS) {
            error("Invalid client ID received in the response, can't proceed, received: %s", clientId);
            return null;
        }

        return clientId;
    }
}
