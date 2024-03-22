package kerbefake.models.auth_server.requests.get_sym_key;

import kerbefake.Utils;
import kerbefake.errors.InvalidMessageException;
import kerbefake.models.auth_server.AuthServerMessageBody;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

public class GetSymmetricKeyRequestBody extends AuthServerMessageBody {

    private byte[] serverId;

    private byte[] nonce;

    public GetSymmetricKeyRequestBody() {
    }

    public byte[] getServerId() {
        return serverId;
    }

    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public AuthServerMessageBody parse(byte[] bodyBytes) throws Exception {
        if (bodyBytes.length != 24) { // 16 byte server ID, 8 byte nonce
            throw new InvalidMessageException(String.format("Invalid size for body bytes, expected 24, got %d", bodyBytes.length));
        }

        this.serverId = byteArrayToLEByteBuffer(bodyBytes, 0, 16).array();
        this.nonce = byteArrayToLEByteBuffer(bodyBytes, 16, 8).array();

        return this;
    }

    @Override
    public byte[] toLEByteArray() {
        byte[] byteArr = new byte[24];
        if (this.serverId == null || this.nonce == null || this.serverId.length != 16 || this.nonce.length != 8) {
            throw new RuntimeException("Missing or invalid values for nonce / server ID.");
        }
        System.arraycopy(this.serverId, 0, byteArr, 0, 16);
        System.arraycopy(this.nonce, 0, byteArr, 16, 8);


        return byteArrayToLEByteBuffer(byteArr).array();
    }
}