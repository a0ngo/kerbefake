package kerbefake.client.operations;

import kerbefake.auth_server.entities.requests.get_sym_key.CreateSymmetricKeyRequestFactory;
import kerbefake.auth_server.entities.requests.get_sym_key.GetSymmetricKeyRequest;
import kerbefake.auth_server.entities.responses.get_sym_key.GetSymmetricKeyResponse;
import kerbefake.auth_server.entities.responses.get_sym_key.GetSymmetricKeyResponseBody;
import kerbefake.client.ClientConnection;
import kerbefake.common.entities.EncryptedKey;
import kerbefake.common.entities.Ticket;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.common.Constants.NONCE_SIZE;
import static kerbefake.common.CryptoUtils.getSecureRandomBytes;
import static kerbefake.common.Logger.error;
import static kerbefake.common.Utils.hexStringToByteArray;

public class GetSymKeyOperation extends ClientOperation<GetSymmetricKeyRequest, GetSymmetricKeyResponse, GetSymmetricKeyResponse> {

    private final String serverId;

    public GetSymKeyOperation(ClientConnection connection, String serverId, String clientId) {
        super(connection, GetSymmetricKeyResponse.class, clientId);
        this.serverId = serverId;
    }

    @Override
    protected GetSymmetricKeyRequest generateRequest() throws InvalidMessageException {
        if (hexStringToByteArray(serverId) == null) {
            throw new InvalidMessageException("Server ID is not a hex string.");
        }

        byte[] nonce = getSecureRandomBytes(NONCE_SIZE);

        return CreateSymmetricKeyRequestFactory.getInstance().setNonce(nonce).setServerId(serverId).setClientId(clientId).build();
    }

    @Override
    protected GetSymmetricKeyResponse validateResponse(GetSymmetricKeyResponse response) {
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
        return response;
    }
}
