package kerbefake.models;

import kerbefake.Utils;
import kerbefake.errors.InvalidMessageException;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

public class EncryptedKey {

    private byte[] iv;

    private byte[] nonce;

    private byte[] aesKey;

    private EncryptedKey() {
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

    public static EncryptedKey parse(byte[] bytes) throws InvalidMessageException {
        if (bytes.length != 56) {
            throw new InvalidMessageException("Enc key byte size must be 56");
        }

        byte[] encKeyIv = byteArrayToLEByteBuffer(bytes, 0, 16).array();
        byte[] nonce = byteArrayToLEByteBuffer(bytes, 16, 8).array();
        byte[] aesKey = byteArrayToLEByteBuffer(bytes, 24, 32).array();

        return new EncryptedKey().setAesKey(aesKey).setNonce(nonce).setIv(encKeyIv);

    }

    public byte[] toLEByteArray() {
        if (iv == null || iv.length != 16 || nonce == null || nonce.length != 8 || aesKey == null || aesKey.length != 32) {
            throw new RuntimeException("Encrypted key structure is invalid and missing one or more value.");
        }

        byte[] byteArr = new byte[56];

        System.arraycopy(iv, 0, byteArr, 0, 16);
        System.arraycopy(nonce, 0, byteArr, 16, 8);
        System.arraycopy(aesKey, 0, byteArr, 24, 32);

        return byteArrayToLEByteBuffer(byteArr).array();
    }

}
