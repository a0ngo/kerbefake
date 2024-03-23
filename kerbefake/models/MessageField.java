package kerbefake.models;

/**
 * Indicates that a class s a field that is sent as part of a message.
 * We can enforce encrypt and decrypt as needed
 */
public interface MessageField extends Message {

    /**
     * Encrypts this field
     *
     * @param key - the key to use for encryption
     * @return - true if encryption was successful, false otherwise.
     */
    boolean encrypt(byte[] key);

    /**
     * Decrypts this field
     *
     * @param key - the key to use for encryption
     * @return - true if decryption was successful, false otherwise.
     */
    boolean decrypt(byte[] key);

}
