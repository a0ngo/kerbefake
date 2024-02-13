package kerbefake;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        return ByteBuffer.wrap(bytes, offset, length).order(ByteOrder.LITTLE_ENDIAN);
    }

    public static String getNullTerminatedStringFromByteArray(byte[] bytes) {
        return getNullTerminatedStringFromByteArray(bytes, 0);
    }

    /**
     * Reads bytes from the byte array until it encountered a null terminator (0x00) and returns the result as a String
     *
     * @param bytes  - the bytes to convert
     * @param offset - the offset from which to start scanning.
     * @return A string that was found to be null terminated in the request - null if no such string was found.
     */
    public static String getNullTerminatedStringFromByteArray(byte[] bytes, int offset) {
        byte[] bytesToScan = byteArrayToLEByteBuffer(bytes).array();
        StringBuilder strBuilder = new StringBuilder();
        boolean nullTerminated = false;
        for (int i = offset; i < bytesToScan.length; i++) {
            if (bytesToScan[i] == 0x00) {
                nullTerminated = true;
                break;
            }
            strBuilder.append((char) bytesToScan[i]);
        }
        if (!nullTerminated) {
            return null;
        }
        return strBuilder.toString();
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
}
