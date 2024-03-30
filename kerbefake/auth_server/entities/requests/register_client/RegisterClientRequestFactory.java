package kerbefake.auth_server.entities.requests.register_client;

import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.MessageFactory;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.common.Constants.ID_HEX_LENGTH_CHARS;
import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.Utils.bytesToHexString;

/**
 * A class used to create {@link RegisterClientRequest}s
 */
public final class RegisterClientRequestFactory extends MessageFactory<RegisterClientRequest> {

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
    protected RegisterClientRequest internalBuild() throws InvalidMessageException {
        if (name == null || name.isEmpty()) {
            throw new InvalidMessageException("Missing name argument for request.");
        }
        if (password == null || password.length == 0) {
            throw new InvalidMessageException("Missing password argument for request.");
        }

        ServerMessageHeader header = new ServerMessageHeader(SERVER_VERSION, MessageCode.REGISTER_CLIENT, payloadSize);
        return new RegisterClientRequest(header, new RegisterClientRequestBody(name, password));
    }

    private static RegisterClientRequestFactory instance;

    public static RegisterClientRequestFactory getInstance() {
        return instance == null ? new RegisterClientRequestFactory() : instance;
    }

    private RegisterClientRequestFactory() {
        instance = this;
        clientId = bytesToHexString(new byte[ID_HEX_LENGTH_CHARS / 2]);
    }
}
