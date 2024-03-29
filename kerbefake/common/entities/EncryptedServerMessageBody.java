package kerbefake.common.entities;

import kerbefake.common.errors.InvalidMessageException;

/**
 * Represents a part of the messsage body (response or request) that is encrypted and can be encrypted or decrypted.
 */
public abstract class EncryptedServerMessageBody extends ServerMessageBody {

    /**
     * A byte array representing the object after being encrypted.
     */
    protected byte[] encryptedData;

    /**
     * Encrypts this object with a provided key
     *
     * @param key - the key to use for encryption
     * @return - true if successful, false otherwise
     */
    public abstract boolean encrypt(byte[] key);

    /**
     * Decrypts the object with a provided key
     *
     * @param key - the key to use for decryption
     * @return - true if successful, false otherwise
     */
    public abstract boolean decrypt(byte[] key) throws InvalidMessageException;

}
