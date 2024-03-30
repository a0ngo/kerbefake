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
        String nameToStore = name;
        if (name.charAt(name.length() - 1) != (char) 0) {
            nameToStore = name + "\0";
        }

        if (this.name != null) payloadSize -= this.name.length();
        this.name = nameToStore;
        payloadSize += nameToStore.length();
        return this;
    }

    public RegisterClientRequestFactory setPassword(char[] password) {
        char[] passwordToStore = password;
        if (password[password.length - 1] != (char) 0) {
            passwordToStore = new char[password.length + 1];
            System.arraycopy(password, 0, passwordToStore, 0, password.length);
            passwordToStore[password.length] = (char) 0;
        }

        if (this.password != null) payloadSize -= this.password.length;
        this.password = passwordToStore;
        payloadSize += passwordToStore.length;
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

        ServerMessageHeader header = new ServerMessageHeader(clientId, SERVER_VERSION, MessageCode.REGISTER_CLIENT, payloadSize);
        return new RegisterClientRequest(header, new RegisterClientRequestBody(name, password));
    }

    // For a register client request we don't care about the client id so we set it to be all zeros and we override
    // build to avoid failing on the non-null client id check.
    @Override
    public RegisterClientRequest build() throws InvalidMessageException {
        if (clientId == null)
            clientId = bytesToHexString(new byte[ID_HEX_LENGTH_CHARS / 2]);
        return internalBuild();
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
