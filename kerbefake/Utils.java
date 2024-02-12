package kerbefake;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
}
