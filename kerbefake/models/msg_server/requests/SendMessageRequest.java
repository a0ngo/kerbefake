package kerbefake.models.msg_server.requests;

import kerbefake.errors.InvalidMessageException;
import kerbefake.models.*;

public class SendMessageRequest extends EncryptedServerMessage implements ServerRequest {
    public SendMessageRequest(ServerMessageHeader header, SendMessageRequestBody body) {
        super(header, body);
    }

    @Override
    public ServerMessage execute() {
        if (this.body == null) {
            throw new RuntimeException("No body provided to send message request.");
        }

        SendMessageRequestBody body = (SendMessageRequestBody) this.body;
        if (body.isEncrypted()) {
            throw new RuntimeException("Message was not decrypted before execution");
        }
        System.out.printf("Message from user (%s): %s", this.header.getClientID(), body.getMessage());

        return new EmptyResponse(this.header.toResponseHeader(MessageCode.SEND_MESSAGE_SUCCESS, 0));
    }

    @Override
    public void encrypt(byte[] key) {
        SendMessageRequestBody body = (SendMessageRequestBody) this.body;
        if (!body.encrypt(key)) {
            throw new RuntimeException("Failed to encrypt message.");
        }
    }

    @Override
    public void decrypt(byte[] key) throws InvalidMessageException {
        SendMessageRequestBody body = (SendMessageRequestBody) this.body;
        if (!body.decrypt(key)) {
            throw new RuntimeException("Failed to decrypt message.");
        }
    }
}
