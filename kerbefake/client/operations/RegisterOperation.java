package kerbefake.client.operations;

import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequest;
import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequestFactory;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponse;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponseBody;
import kerbefake.client.ClientConnection;
import kerbefake.common.errors.InvalidMessageException;

import java.util.Arrays;

import static kerbefake.client.UserInputOutputHandler.getNameFromUser;
import static kerbefake.common.Constants.ID_HEX_LENGTH_CHARS;
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
    protected RegisterClientRequest generateRequest() throws InvalidMessageException {
        String name = getNameFromUser();

        RegisterClientRequest request = RegisterClientRequestFactory.getInstance().setName(name).setPassword(plaintextPassword).build();
        Arrays.fill(plaintextPassword, (char) 0);
        return request;
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
