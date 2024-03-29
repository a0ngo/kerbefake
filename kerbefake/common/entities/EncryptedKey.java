package kerbefake.common.entities;

import kerbefake.common.Utils;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.common.Constants.NONCE_SIZE;
import static kerbefake.common.Logger.error;
import static kerbefake.common.Utils.*;

public class EncryptedKey extends EncryptedServerMessageBody {

    private byte[] iv;

    private byte[] nonce;

    private byte[] aesKey;

    public EncryptedKey() {
    }

    public EncryptedKey setIv(byte[] iv) {
        this.iv = iv;
        return this;
    }

    public EncryptedKey setNonce(byte[] nonce) {
        this.nonce = nonce;
        return this;
    }

    public EncryptedKey setAesKey(byte[] aesKey) {
        this.aesKey = aesKey;
        return this;
    }

    public boolean isEncrypted() {
        return (this.aesKey == null || this.aesKey.length != 32) || (this.nonce == null || this.nonce.length != NONCE_SIZE);
    }

    @Override
    public boolean decrypt(byte[] key) {
        if (!assertNonZeroedByteArrayOfLengthN(this.iv, 16)) {
            return false;
        }
        try {
            byte[] decryptedData = Utils.decrypt(key, this.iv, this.encryptedData);
            this.nonce = new byte[8];
            this.aesKey = new byte[32];
            System.arraycopy(decryptedData, 0, this.nonce, 0, 8);
            System.arraycopy(decryptedData, 8, this.aesKey, 0, 32);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean encrypt(byte[] key) {
        if (!assertNonZeroedByteArrayOfLengthN(this.iv, 16)) {
            return false;
        }
        if (this.nonce == null || this.aesKey == null || this.nonce.length != 8 || this.aesKey.length != 32) {
            error("Missing nonce or aes key for encryption.");
            return false;
        }

        boolean nonceZeroed = true;
        for (byte b : this.nonce) nonceZeroed &= (b == 0);
        if (nonceZeroed) {
            error("Nonce is zeroed can't proceed");
            return false;
        }

        // TODO: Do we allow a zeroed AES? it might imply a lack of initialization
        try {
            byte[] dataToEncrypt = new byte[40]; // Nonce + AES
            System.arraycopy(nonce, 0, dataToEncrypt, 0, 8);
            System.arraycopy(aesKey, 0, dataToEncrypt, 8, 32);

            this.encryptedData = Utils.encrypt(key, this.iv, dataToEncrypt);
            return true;
        } catch (RuntimeException e) {
            return false;
        }

    }

    @Override
    public EncryptedKey parse(byte[] bytes) throws InvalidMessageException {
        // We do not perform size enforcement, we get the first 16 bytes as the IV and then decrypt the message and only then we set all the values.
        byte[] encKeyIv = byteArrayToLEByteBuffer(bytes, 0, 16).array();
        byte[] encryptedData = byteArrayToLEByteBuffer(bytes, 16, bytes.length - 16).array();
        EncryptedKey encKey = new EncryptedKey().setAesKey(aesKey).setNonce(nonce).setIv(encKeyIv);
        encKey.encryptedData = encryptedData;
        return encKey;
    }

    @Override
    public byte[] toLEByteArray() {
        if (iv == null || iv.length != 16) {
            throw new RuntimeException("Encrypted key structure is invalid and missing one or more value - make sure to run encrypt before converting to LE byte array.");
        }

        if (this.encryptedData == null || this.encryptedData.length < 48) {
            throw new RuntimeException("Encrypted key must be encrypted before sending.");
        }

        byte[] bytes = new byte[16 + this.encryptedData.length]; // 16 byte IV  + encrypted data
        System.arraycopy(iv, 0, bytes, 0, 16);
        System.arraycopy(this.encryptedData, 0, bytes, 16, this.encryptedData.length);

        return byteArrayToLEByteBuffer(bytes).array();
    }

    public byte[] getNonce() {
        return nonce;
    }

    public byte[] getAesKey() {
        return aesKey;
    }
}
