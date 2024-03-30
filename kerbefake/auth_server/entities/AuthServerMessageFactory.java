package kerbefake.auth_server.entities;

import kerbefake.auth_server.entities.requests.get_sym_key.GetSymmetricKeyRequest;
import kerbefake.auth_server.entities.requests.get_sym_key.GetSymmetricKeyRequestBody;
import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequest;
import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequestBody;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.MessageFactory;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.common.Constants.NONCE_SIZE;
import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.Utils.assertNonZeroedByteArrayOfLengthN;

/**
 * A class that provides the functionality of building auth server specific messages (not responses!).
 */
public final class AuthServerMessageFactory {

    private static AuthServerMessageFactory instance;

    public static AuthServerMessageFactory getInstance() {
        return instance == null ? new AuthServerMessageFactory() : instance;
    }

    private AuthServerMessageFactory() {
    }

    public RegisterClientRequestFactory createRegisterClientRequest() {
        return new RegisterClientRequestFactory();
    }

    public GetSymmetricKeyRequestFactory createGetSymmetricKeyRequest() {
        return new GetSymmetricKeyRequestFactory();
    }


    /**
     * A class used to create {@link RegisterClientRequest}s
     */
    private static final class RegisterClientRequestFactory extends MessageFactory<RegisterClientRequest> {

        private String name;

        private char[] password;

        public RegisterClientRequestFactory setName(String name) {
            if (this.name != null) payloadSize -= this.name.length();
            this.name = name;
            payloadSize += name.length();
            return this;
        }

        public RegisterClientRequestFactory setPassword(char[] password) {
            if (this.password != null) payloadSize -= this.password.length;
            this.password = password;
            payloadSize += password.length;
            return this;
        }

        @Override
        public RegisterClientRequest build() throws InvalidMessageException {
            if (name == null || name.isEmpty()) {
                throw new InvalidMessageException("Missing name argument for request.");
            }
            if (password == null || password.length == 0) {
                throw new InvalidMessageException("Missing password argument for request.");
            }

            ServerMessageHeader header = new ServerMessageHeader(SERVER_VERSION, MessageCode.REGISTER_CLIENT, payloadSize);
            return new RegisterClientRequest(header, new RegisterClientRequestBody(name, password));
        }

        public RegisterClientRequestFactory() {
        }
    }

    private static final class GetSymmetricKeyRequestFactory extends MessageFactory<GetSymmetricKeyRequest> {

        private String serverId;

        private byte[] nonce;


        public GetSymmetricKeyRequestFactory setServerId(String serverId) {
            if (this.serverId != null) payloadSize -= this.serverId.length();
            this.serverId = serverId;
            payloadSize += serverId.length();
            return this;
        }

        public GetSymmetricKeyRequestFactory setNonce(byte[] nonce) {
            if (this.nonce != null) payloadSize -= this.nonce.length;
            this.nonce = nonce;
            payloadSize += nonce.length;
            return this;
        }


        public GetSymmetricKeyRequestFactory() {
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
    }


}
