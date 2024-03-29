package kerbefake;

import kerbefake.errors.CryptographicException;
import kerbefake.errors.InvalidHexStringException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static kerbefake.Logger.error;

/**
 * A bunch of useful utility functions
 */
public final class Utils {

    /**
     * Returns a LE (Little Endian) byte buffer.
     *
     * @param bytes - the bytes to return
     * @return a ByteBuffer with the contents of `bytes` as LE.
     */
    public static ByteBuffer byteArrayToLEByteBuffer(byte[] bytes) {
        return byteArrayToLEByteBuffer(bytes, 0, bytes.length);
    }

    /**
     * Converts a byte array to a {@link ByteBuffer} from the specific offset for the specified length
     *
     * @param bytes  - the bytes to convert
     * @param offset - the offset from which to start
     * @param length - the length of the new ByteBuffer
     * @return a {@link ByteBuffer} in Little Endian format.
     */
    public static ByteBuffer byteArrayToLEByteBuffer(byte[] bytes, int offset, int length) {
        byte[] bytesToCopy;
        if (bytes.length != length) {
            bytesToCopy = Arrays.copyOfRange(bytes, offset, offset + length);
        } else {
            bytesToCopy = bytes;
        }
        return ByteBuffer.wrap(bytesToCopy).order(ByteOrder.LITTLE_ENDIAN);
    }

    public static String getNullTerminatedStringFromByteArray(byte[] bytes) {
        char[] chars = getNullTerminatedCharArrayFromByteArray(bytes, 0);
        return chars == null ? null : new String(chars);
    }

    /**
     * Reads bytes from the byte array until it encountered a null terminator (0x00) and returns the result as a String
     *
     * @param bytes  - the bytes to convert
     * @param offset - the offset from which to start scanning.
     * @return A char array that was found to be null terminated in the request - null if no such string was found.
     */
    public static char[] getNullTerminatedCharArrayFromByteArray(byte[] bytes, int offset) {
        byte[] bytesToScan = byteArrayToLEByteBuffer(bytes).array();
        List<Character> charsList = new ArrayList<>();
        boolean nullTerminated = false;
        for (int i = offset; i < bytesToScan.length; i++) {
            if (bytesToScan[i] == 0x00) {
                nullTerminated = true;
                break;
            }
            charsList.add((char) bytesToScan[i]);
        }
        if (!nullTerminated) {
            return null;
        }
        char[] chars = new char[charsList.size()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = charsList.get(i);
        }
        return chars;
    }

    /**
     * Formats a given date into the server's format.
     *
     * @param date - the date to format
     * @return a string representing the date
     */
    public static String getFormattedDate(Date date) {
        return new SimpleDateFormat(Constants.DATE_FORMAT).format(date);
    }

    /**
     * Transforms a hex string to a normal string.
     *
     * @param hexStr - the hex string
     * @return a normal utf-8 string
     */
    public static String hexStrToStr(String hexStr) {
        assert hexStr.length() % 2 == 0;
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < hexStr.length(); i += 2) {
            strBuilder.append((char) Integer.parseInt(hexStr.substring(i, i + 2), 16));
        }
        return strBuilder.toString();
    }

    /**
     * Converts a string to a little endian byte array
     *
     * @param str - the string to convert
     * @return - a byte array of length {@code str.length} of LE data for the provided str.
     */
    public static byte[] strToLEByteArray(String str) {
        return ByteBuffer.allocate(str.length()).order(ByteOrder.LITTLE_ENDIAN).put(str.getBytes(StandardCharsets.US_ASCII)).array();
    }

    /**
     * Converts an int to a little endian byte array.
     *
     * @param i - the integer to convert
     * @return a byte array of length 4 of LE data for the provided int.
     */
    public static byte[] intToLEByteArray(int i) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i).array();
    }

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
            error("Failed to perform crypto operation, this can be because this PC does not support the needed ciphers (AES/CBC/PKCS5Padding) or that the wrong key was used for decryption");
            throw new CryptographicException(e);
        }
    }

    /**
     * Verifies that an array of bytes is of a specific length, is not null and is not all zeros.
     *
     * @param arr - the array to check
     * @param n   - the expected length
     * @return - true if it is not zero and is of the specified length.
     */
    public static boolean assertNonZeroedByteArrayOfLengthN(byte[] arr, int n) {
        if (arr == null || arr.length != n) {
            return false;
        }

        boolean zeroed = true;
        for (byte b : arr) zeroed &= (b == 0);

        return !zeroed;
    }

    /**
     * Converts a byte array to a hex string
     *
     * @param bytes - the bytes to convert
     * @return a hex string corresponding to the byte array
     */
    public static String bytesToHexString(byte[] bytes) {
        final String values = "0123456789abcdef";
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            int firstDigit = (b & 0xff) >> 4;
            int secondDigit = b & 0xff & 0x0f;
            builder.append(values.charAt(firstDigit)).append(values.charAt(secondDigit));
        }
        return builder.toString();
    }

    /**
     * Converts a hex string to a byte array (signed)
     *
     * @param hexString - the hex string to convert
     * @return a byte array corresponding to the hex string
     */
    public static byte[] hexStringToByteArray(String hexString) throws InvalidHexStringException {
        String hex = hexString.length() % 2 == 0 ? hexString : "0" + hexString;
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            char leftDigit = hex.charAt(i);
            char rightDigit = hex.charAt(i + 1);
            if (leftDigit < '0' || (leftDigit > '9' && leftDigit < 'a') || leftDigit > 'f'
                    || rightDigit < '0' || (rightDigit > '9' && rightDigit < 'a') || rightDigit > 'f') {
                throw new InvalidHexStringException(hex, i / 2);
            }
            byte b = (byte) ((Byte.parseByte(String.valueOf(leftDigit), 16) << 4) + Byte.parseByte(String.valueOf(rightDigit), 16));
            bytes[i / 2] = b;
        }
        return bytes;
    }

    /**
     * Performs SHA-256 on a given value and returns the result.
     *
     * @param value - the value to perform a hash on.
     * @return the byte array of the resulting hash
     */
    public static byte[] performSha256OnValue(String value) throws NoSuchAlgorithmException {
        return performSha256OnValue(value.toCharArray());
    }

    /**
     * Performs SHA-256 on a given value and returns the result.
     *
     * @param value - the value to perform a hash on.
     * @return the byte array of the resulting hash
     */
    public static byte[] performSha256OnValue(char[] value) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            bytes[i] = (byte) value[i];
        }
        return digest.digest(bytes);
    }


}
