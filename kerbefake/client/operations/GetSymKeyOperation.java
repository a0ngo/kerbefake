package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.common.entities.*;
import kerbefake.common.errors.InvalidHexStringException;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.auth_server.entities.requests.get_sym_key.GetSymmetricKeyRequest;
import kerbefake.auth_server.entities.requests.get_sym_key.GetSymmetricKeyRequestBody;
import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.auth_server.entities.responses.get_sym_key.GetSymmetricKeyResponse;
import kerbefake.auth_server.entities.responses.get_sym_key.GetSymmetricKeyResponseBody;

import java.io.IOException;
import java.security.SecureRandom;

import static kerbefake.common.Constants.ClientConstants.REQUEST_FAILED;
import static kerbefake.common.Constants.NONCE_SIZE;
import static kerbefake.common.Constants.ResponseCodes.UNKNOWN_FAILURE_CODE;
import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.Logger.error;
import static kerbefake.common.Utils.hexStringToByteArray;

public class GetSymKeyOperation extends ClientOperation<GetSymmetricKeyRequest, GetSymmetricKeyResponse> {

    private String serverId;

    public GetSymKeyOperation(ClientConnection connection, String serverId) {
        super(connection);
        this.serverId = serverId;
    }

    @Override
    public GetSymmetricKeyResponse perform() {
        try {
            hexStringToByteArray(serverId);
        } catch (InvalidHexStringException e) {
            error("Provided server ID is not a hex string");
            return null;
        }

        try {
            // Generate a random nonce.
            byte[] nonce = new byte[NONCE_SIZE];
            SecureRandom srand = new SecureRandom();
            srand.nextBytes(nonce);


            GetSymmetricKeyRequestBody getSymmetricKeyRequestBody = new GetSymmetricKeyRequestBody(serverId, nonce);
            ServerMessageHeader serverMessageHeader = new ServerMessageHeader(SERVER_VERSION, MessageCode.REQUEST_SYMMETRIC_KEY, getSymmetricKeyRequestBody.toLEByteArray().length);
            GetSymmetricKeyRequest getSymmetricKeyRequest = new GetSymmetricKeyRequest(serverMessageHeader, getSymmetricKeyRequestBody);

            ServerMessage response = this.internalPerform(getSymmetricKeyRequest);

            if (response instanceof FailureResponse) {
                if (response.getHeader().getMessageCode().getCode() == UNKNOWN_FAILURE_CODE) {
                    error(REQUEST_FAILED);
                    return null;
                }
                error("Received unknown response code: %d - can't proceed.", response.getHeader().getMessageCode().getCode());
                return null;
            }

            if (!(response instanceof GetSymmetricKeyResponse)) {
                error("Received unknown response, can't proceed, response is of type %s, printing response:\n %s", response.getClass().getCanonicalName(), response.toString());
                return null;
            }

            if (response.getBody() == null) {
                error("Received response but missing response body, can't proceed, please contact server admin.");
                return null;
            }

            GetSymmetricKeyResponseBody responseBody = (GetSymmetricKeyResponseBody) response.getBody();
            Ticket responseTicket = responseBody.getTicket();
            EncryptedKey encKey = responseBody.getEncKey();

            if (responseTicket == null || encKey == null) {
                error("Missing encrypted key or ticket in response from server.");
                return null;
            }
            return (GetSymmetricKeyResponse) response;
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
