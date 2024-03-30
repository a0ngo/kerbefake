package kerbefake.common.entities;

import kerbefake.common.errors.InvalidMessageException;

/**
 * General interface for any message sent back and forth.
 */
public interface Message {

    /**
     * Converts a given message to an LE byte array.
     *
     * @return this message but in a little endian byte array.
     */
    public byte[] toLEByteArray() throws InvalidMessageException;
}
