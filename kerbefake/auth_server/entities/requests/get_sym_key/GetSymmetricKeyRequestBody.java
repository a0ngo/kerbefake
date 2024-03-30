package kerbefake.auth_server.entities.requests.get_sym_key;

import kerbefake.common.entities.ServerMessageBody;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.common.Constants.ID_HEX_LENGTH_CHARS;
import static kerbefake.common.Utils.*;

public class GetSymmetricKeyRequestBody extends ServerMessageBody {

    private String serverId;

    private byte[] nonce;

    public GetSymmetricKeyRequestBody() {
    }

    public GetSymmetricKeyRequestBody(String serverId, byte[] nonce) {
        this.serverId = serverId;
        this.nonce = nonce;
    }

    public String getServerId() {
        return serverId;
    }

    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public ServerMessageBody parse(byte[] bodyBytes) throws Exception {
        if (bodyBytes.length != 24) { // 16 byte server ID, 8 byte nonce
            throw new InvalidMessageException(String.format("Invalid size for body bytes, expected 24, got %d", bodyBytes.length));
        }

        this.serverId = bytesToHexString(byteArrayToLEByteBuffer(bodyBytes, 0, 16).array());
        this.nonce = byteArrayToLEByteBuffer(bodyBytes, 16, 8).array();

        return this;
    }

    @Override
    public byte[] toLEByteArray() throws InvalidMessageException {
        byte[] byteArr = new byte[24];
        if (this.serverId == null || this.nonce == null || this.serverId.length() != 32 || this.nonce.length != 8) {
            throw new RuntimeException("Missing or invalid values for nonce / server ID.");
        }
        byte[] serverIdBytes = hexStringToByteArray(this.serverId);
        if (serverIdBytes == null) {
            throw new InvalidMessageException("Server ID is not a hex string");
        }
        System.arraycopy(serverIdBytes, 0, byteArr, 0, ID_HEX_LENGTH_CHARS / 2);
        System.arraycopy(this.nonce, 0, byteArr, ID_HEX_LENGTH_CHARS / 2, 8);


        return byteArrayToLEByteBuffer(byteArr).array();
    }
}
