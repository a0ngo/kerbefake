package kerbefake.common.entities;

import kerbefake.common.CryptoUtils;
import kerbefake.common.Logger;
import kerbefake.common.errors.InvalidMessageException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static kerbefake.common.Logger.commonLogger;
import static kerbefake.common.Utils.*;

public class Authenticator extends EncryptedServerMessageBody {

    // Data to encrypt is 41 bytes -> 48 bytes with padding.
    public static final int DATA_ENCRYPTED_SIZE = 48;

    // 1 - version + 16 - server ID + 16 - client ID + 8 creation time.
    public static final int DATA_DECRYPTED_SIZE = 41;

    private byte[] iv;

    private byte version;

    private byte[] clientIdBytes;

    private byte[] serverIdBytes;

    private byte[] creationTime;

    public Authenticator() {
    }


    public Authenticator(byte[] iv, String clientId, String serverId, long creationTime) {
        this(iv, hexStringToByteArray(clientId), hexStringToByteArray(serverId), ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(creationTime).array());
    }

    public Authenticator(byte[] iv, byte[] clientIdBytes, byte[] serverIdBytes, byte[] creationTime) {
        this.version = 24;
        this.iv = iv;
        this.clientIdBytes = clientIdBytes;
        this.serverIdBytes = serverIdBytes;
        this.creationTime = creationTime;
    }

    @Override
    public Authenticator parse(byte[] bodyBytes) throws Exception {
        if (bodyBytes.length != 16 + DATA_ENCRYPTED_SIZE) {
            throw new InvalidMessageException(String.format("Message length is not sufficient (%d but should be %d when encrypted).", DATA_ENCRYPTED_SIZE, bodyBytes.length));
        }
        this.iv = new byte[16];
        System.arraycopy(bodyBytes, 0, iv, 0, 16);
        this.encryptedData = new byte[48];
        System.arraycopy(bodyBytes, 16, this.encryptedData, 0, 48);
        return this;
    }

    @Override
    public byte[] toLEByteArray() {
        if (this.iv == null || this.iv.length != 16) {
            throw new RuntimeException("IV is missing or is of invalid length");
        }

        if (!assertNonZeroedByteArrayOfLengthN(this.encryptedData, DATA_ENCRYPTED_SIZE)) {
            throw new RuntimeException("Encrypted data is missing or is of invalid length");
        }

        byte[] bytes = new byte[16 + this.encryptedData.length];
        System.arraycopy(this.iv, 0, bytes, 0, 16);
        System.arraycopy(this.encryptedData, 0, bytes, 16, this.encryptedData.length);

        return byteArrayToLEByteBuffer(bytes).array();
    }

    @Override
    public boolean encrypt(byte[] key) {
        if (!assertNonZeroedByteArrayOfLengthN(this.iv, 16)) {
            throw new RuntimeException("IV is not initialized or is 0");
        }

        try {
            byte[] dataToEncrypt = new byte[DATA_DECRYPTED_SIZE];
            dataToEncrypt[0] = this.version;
            int offset = 1;
            System.arraycopy(this.clientIdBytes, 0, dataToEncrypt, offset, this.clientIdBytes.length);
            offset += this.clientIdBytes.length;
            System.arraycopy(this.serverIdBytes, 0, dataToEncrypt, offset, this.serverIdBytes.length);
            offset += this.serverIdBytes.length;
            System.arraycopy(this.creationTime, 0, dataToEncrypt, offset, this.creationTime.length);

            this.encryptedData = CryptoUtils.encrypt(key, this.iv, dataToEncrypt);
            return true;
        } catch (RuntimeException e) {
            commonLogger.error("Encryption failed due to: %s", e);
            return false;
        }
    }

    @Override
    public boolean decrypt(byte[] key) throws InvalidMessageException {
        if (!assertNonZeroedByteArrayOfLengthN(this.encryptedData, DATA_ENCRYPTED_SIZE)) {
            throw new RuntimeException("Encrypted data is missing or is of invalid length (at least 64 bytes");
        }

        if (!assertNonZeroedByteArrayOfLengthN(this.iv, 16)) {
            throw new RuntimeException("IV is missing or is 0.");
        }
        try {
            byte[] decryptedData = CryptoUtils.decrypt(key, this.iv, this.encryptedData);
            if (decryptedData.length != DATA_DECRYPTED_SIZE) {
                commonLogger.error("Invalid decryption size, expected %d got %d", DATA_DECRYPTED_SIZE, decryptedData.length);
                return false;
            }
            this.version = decryptedData[0];
            this.clientIdBytes = new byte[16];
            this.serverIdBytes = new byte[16];
            this.creationTime = new byte[8];
            int offset = 1;
            System.arraycopy(decryptedData, offset, this.clientIdBytes, 0, clientIdBytes.length);
            offset += clientIdBytes.length;
            System.arraycopy(decryptedData, offset, this.serverIdBytes, 0, serverIdBytes.length);
            offset += serverIdBytes.length;
            System.arraycopy(decryptedData, offset, this.creationTime, 0, creationTime.length);

            return true;
        } catch (RuntimeException e) {
            commonLogger.error("Decryption failed due to: %s", e);
            return false;
        }
    }
}
