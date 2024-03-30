package kerbefake.msg_server.entities;

import kerbefake.common.CryptoUtils;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.MessageFactory;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.errors.InvalidMessageException;

import java.nio.charset.StandardCharsets;

import static kerbefake.common.Constants.SERVER_VERSION;
import static kerbefake.common.CryptoUtils.getIv;
import static kerbefake.common.Utils.assertNonZeroedByteArrayOfLengthN;

public final class SendMessageRequestFactory extends MessageFactory<SendMessageRequest> {

    private int messageSize;
    private byte[] iv;
    private byte[] encryptedMessage;
    private String message;
    private boolean encrypted;

    private static SendMessageRequestFactory instance;

    public static SendMessageRequestFactory getInstance() {
        return instance == null ? new SendMessageRequestFactory() : instance;
    }

    private SendMessageRequestFactory() {
        instance = this;
    }

    public SendMessageRequestFactory setMessage(String message) {
        this.message = message;
        return this;
    }

    public SendMessageRequestFactory encrypt(byte[] key) throws InvalidMessageException {
        if (message == null || message.isEmpty()) {
            throw new InvalidMessageException("Missing message for request.");
        }

        this.iv = getIv();
        encryptedMessage = CryptoUtils.encrypt(key, iv, message.getBytes(StandardCharsets.UTF_8));
        this.messageSize = encryptedMessage.length;

        payloadSize += 4; // messageSize
        payloadSize += 16; // IV
        payloadSize += encryptedMessage.length;

        encrypted = true;

        return this;
    }

    @Override
    protected SendMessageRequest internalBuild() throws InvalidMessageException {
        if (iv == null || !encrypted)
            throw new InvalidMessageException("Missing IV or not encrypted before building.");
        if (encryptedMessage == null || encryptedMessage.length == 0 || !assertNonZeroedByteArrayOfLengthN(encryptedMessage, encryptedMessage.length))
            throw new InvalidMessageException("Must encrypt before building.");
        ServerMessageHeader header = new ServerMessageHeader(clientId, SERVER_VERSION, MessageCode.SEND_MESSAGE, payloadSize);
        return new SendMessageRequest(header, new SendMessageRequestBody(messageSize, iv, encryptedMessage));
    }
}