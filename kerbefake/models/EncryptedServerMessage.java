package kerbefake.models;

import kerbefake.errors.InvalidMessageException;

public abstract class EncryptedServerMessage extends ServerMessage {

    public EncryptedServerMessage(ServerMessageHeader header, ServerMessageBody body) {
        super(header, body);
    }

    public abstract void encrypt(byte[] key);

    public abstract void decrypt(byte[] key) throws InvalidMessageException;
}
