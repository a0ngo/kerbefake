package kerbefake.msg_server.entities;

import kerbefake.common.CryptoUtils;
import kerbefake.common.entities.EncryptedServerMessageBody;
import kerbefake.common.entities.ServerMessageBody;
import kerbefake.common.errors.InvalidMessageException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static kerbefake.common.Logger.error;
import static kerbefake.common.Utils.assertNonZeroedByteArrayOfLengthN;
import static kerbefake.common.Utils.byteArrayToLEByteBuffer;

public class SendMessageRequestBody extends EncryptedServerMessageBody {


    // Note: this field is pointless, we can find the message size by doing payloadSize - 16
    private int messageSize;
    private byte[] iv;
    private String message;

    public SendMessageRequestBody() {
    }

    public SendMessageRequestBody(byte[] iv, String message) {
        this.iv = iv;
        this.message = message;
    }

    public SendMessageRequestBody(int messageSize, byte[] iv, byte[] encryptedData) {
        this.messageSize = messageSize;
        this.iv = iv;
        this.encryptedData = encryptedData;
    }

    @Override
    public boolean encrypt(byte[] key) {
        if (!assertNonZeroedByteArrayOfLengthN(this.iv, 16)) {
            throw new RuntimeException("IV is not initialized or is 0.");
        }

        try {
            byte[] dataToEncrypt = message.getBytes(StandardCharsets.UTF_8);
            this.encryptedData = CryptoUtils.encrypt(key, this.iv, dataToEncrypt);
            this.messageSize = this.encryptedData.length;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            error("Encryption failed due to: %s", e);
            return false;
        }
    }

    @Override
    public boolean decrypt(byte[] key) throws InvalidMessageException {
        if (!assertNonZeroedByteArrayOfLengthN(this.iv, 16)) {
            throw new RuntimeException("IV is not initialized or is 0.");
        }

        try {
            byte[] decryptedMessage = CryptoUtils.decrypt(key, this.iv, this.encryptedData);
            this.message = new String(decryptedMessage, StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            error("Decryption failed due to: %s", e);
            return false;
        }
    }

    @Override
    public ServerMessageBody parse(byte[] bodyBytes) throws Exception {
        this.messageSize = byteArrayToLEByteBuffer(bodyBytes, 0, 4).getInt();
        this.iv = byteArrayToLEByteBuffer(bodyBytes, 4, 16).array();
        this.encryptedData = byteArrayToLEByteBuffer(bodyBytes, 20, messageSize).array();
        return this;
    }

    @Override
    public byte[] toLEByteArray() {
        if (!assertNonZeroedByteArrayOfLengthN(encryptedData, messageSize)) {
            throw new RuntimeException("Object was not encrypted before conversion to byte array.");
        }
        byte[] bytes = new byte[4 + 16 + messageSize];
        System.arraycopy(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(messageSize).array(), 0, bytes, 0, 4);
        System.arraycopy(iv, 0, bytes, 4, 16);
        System.arraycopy(encryptedData, 0, bytes, 20, encryptedData.length);
        return byteArrayToLEByteBuffer(bytes).array();
    }

    public boolean isEncrypted() {
        return this.encryptedData != null && this.message == null;
    }

    public String getMessage() {
        return message;
    }
}
