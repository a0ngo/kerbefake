package kerbefake.models;

import kerbefake.Utils;
import kerbefake.errors.InvalidMessageException;

import static kerbefake.Logger.error;
import static kerbefake.Utils.assertNonZeroedByteArrayOfLengthN;
import static kerbefake.Utils.byteArrayToLEByteBuffer;

public class EncryptedKey implements MessageField {

    public static final int SIZE = 64;

    private byte[] iv;

    private byte[] nonce;

    private byte[] encNonce;

    private byte[] aesKey;

    private byte[] encAesKey;

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

    private EncryptedKey setEncNonce(byte[] encNonce) {
        this.encNonce = encNonce;
        return this;
    }

    private EncryptedKey setEncAesKey(byte[] encAesKey) {
        this.encAesKey = encAesKey;
        return this;
    }

    @Override
    public boolean decrypt(byte[] key) {
        if (!assertNonZeroedByteArrayOfLengthN(this.iv, 16)) {
            return false;
        }
        try {
            byte[] decNonce = Utils.decrypt(key, this.iv, this.encNonce);
            this.nonce = new byte[8];
            System.arraycopy(decNonce, 0, this.nonce, 0, 8);

            setAesKey(Utils.decrypt(key, this.iv, this.encAesKey));

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
            setEncNonce(Utils.encrypt(key, iv, this.nonce));
            setEncAesKey(Utils.encrypt(key, iv, this.aesKey));

            return true;
        } catch (RuntimeException e) {
            return false;
        }

    }

    public static EncryptedKey parse(byte[] bytes) throws InvalidMessageException {
        /*
        Parsing the encrypted key assumes that the fields aesKey and nonce are encrypted.
        As a result their size is not the same as the size specified in the protocol since nonce is 8 bytes but
        encrypted it'll be 16.
         */
        if (bytes.length != SIZE) {
            throw new InvalidMessageException("Enc key byte size must be 56");
        }

        byte[] encKeyIv = byteArrayToLEByteBuffer(bytes, 0, 16).array();
        byte[] nonce = byteArrayToLEByteBuffer(bytes, 16, 16).array();
        byte[] aesKey = byteArrayToLEByteBuffer(bytes, 32, 32).array();

        return new EncryptedKey().setEncAesKey(aesKey).setEncNonce(nonce).setIv(encKeyIv);
    }


    public byte[] toLEByteArray() {
        if (iv == null || iv.length != 16 || encNonce == null || encNonce.length != 16 || encAesKey == null || encAesKey.length != 32) {
            throw new RuntimeException("Encrypted key structure is invalid and missing one or more value - make sure to run encrypt before converting to LE byte array.");
        }

        byte[] byteArr = new byte[SIZE];

        System.arraycopy(iv, 0, byteArr, 0, 16);
        System.arraycopy(encNonce, 0, byteArr, 16, 16);
        System.arraycopy(encAesKey, 0, byteArr, 24, 32);

        return byteArrayToLEByteBuffer(byteArr).array();
    }

}
