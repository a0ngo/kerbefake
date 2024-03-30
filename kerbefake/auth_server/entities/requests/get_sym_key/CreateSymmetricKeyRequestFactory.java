package kerbefake.auth_server.entities.requests.get_sym_key;

import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.MessageFactory;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.common.Constants.NONCE_SIZE;
import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.Utils.assertNonZeroedByteArrayOfLengthN;

public class CreateSymmetricKeyRequestFactory extends MessageFactory<GetSymmetricKeyRequest> {

    private String serverId;

    private byte[] nonce;


    public CreateSymmetricKeyRequestFactory setServerId(String serverId) {
        if (this.serverId != null) payloadSize -= this.serverId.length();
        this.serverId = serverId;
        payloadSize += serverId.length();
        return this;
    }

    public CreateSymmetricKeyRequestFactory setNonce(byte[] nonce) {
        if (this.nonce != null) payloadSize -= this.nonce.length;
        this.nonce = nonce;
        payloadSize += nonce.length;
        return this;
    }

    @Override
    public GetSymmetricKeyRequest build() throws InvalidMessageException {
        if (serverId == null || serverId.isEmpty()) {
            throw new InvalidMessageException("Missing server ID from request.");
        }
        if (assertNonZeroedByteArrayOfLengthN(nonce, NONCE_SIZE)) {
            throw new InvalidMessageException("Missing nonce from request or is all 0.");
        }
        ServerMessageHeader header = new ServerMessageHeader(SERVER_VERSION, MessageCode.REQUEST_SYMMETRIC_KEY, payloadSize);
        return new GetSymmetricKeyRequest(header, new GetSymmetricKeyRequestBody(serverId, nonce));
    }

    private CreateSymmetricKeyRequestFactory() {
        instance = this;
    }

    private static CreateSymmetricKeyRequestFactory instance;

    public static CreateSymmetricKeyRequestFactory getInstance() {
        return instance == null ? new CreateSymmetricKeyRequestFactory() : instance;
    }

}
