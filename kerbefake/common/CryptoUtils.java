package kerbefake.common;

import kerbefake.common.errors.CryptographicException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

import static kerbefake.common.Logger.commonLogger;

/**
 * A class containing cryptographic utility functions
 */
public final class CryptoUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Given a key an IV encrypt a value provided.
     *
     * @param key            - the key to use for encryption
     * @param iv             - the IV to use for encryption
     * @param valueToEncrypt - the value to encrypt
     * @return - a byte array of the value encrypted.
     */
    public static byte[] encrypt(byte[] key, byte[] iv, byte[] valueToEncrypt) {
        return performCryptoOp(key, iv, valueToEncrypt, true);
    }

    /**
     * Given a key and an IV decrypt a value provided.
     *
     * @param key            - the key to use for decryption
     * @param iv             - the IV to use for decryption
     * @param valueToDecrypt - the value to decrypt
     * @return - a byte array of the decrypted value.
     */
    public static byte[] decrypt(byte[] key, byte[] iv, byte[] valueToDecrypt) {
        return performCryptoOp(key, iv, valueToDecrypt, false);
    }

    /**
     * Performs an encryption or decryption on a given value using the provided key and IV.
     *
     * @param key   - the key to use
     * @param iv    - the IV to use
     * @param value - the value to perform the operation on
     * @param enc   - a flag to mark encryption, if false will decrypt
     * @return - a byte array with the relevant value.
     */
    private static byte[] performCryptoOp(byte[] key, byte[] iv, byte[] value, boolean enc) {
        SecretKey secret = new SecretKeySpec(key, "AES");
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(enc ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            return cipher.doFinal(value);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            commonLogger.errorToFileOnly("Failed to perform crypto operation, this can be because this PC does not support the needed ciphers (AES/CBC/PKCS5Padding) or that the wrong key was used for decryption");
            throw new CryptographicException(e);
        }
    }


    /**
     * Performs SHA-256 on a given value and returns the result.
     *
     * @param value - the value to perform a hash on.
     * @return the byte array of the resulting hash
     */
    public static byte[] performSha256(String value) throws NoSuchAlgorithmException {
        return performSha256(value.toCharArray());
    }

    /**
     * Performs SHA-256 on a given value and returns the result.
     *
     * @param value - the value to perform a hash on.
     * @return the byte array of the resulting hash
     */
    public static byte[] performSha256(char[] value) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            bytes[i] = (byte) value[i];
        }
        return digest.digest(bytes);
    }

    /**
     * Generates a random IV from a secure source
     *
     * @return a byte array of {@link Constants#IV_SIZE} size.
     */
    public static byte[] getIv() {
        return getSecureRandomBytes(16);
    }

    /**
     * Generates random bytes from a secure source with a given size
     *
     * @param size - the size requested
     * @return the byte array
     */
    public static byte[] getSecureRandomBytes(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Requested a negative size of byte array.");
        }

        byte[] bytes = new byte[size];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }


}
