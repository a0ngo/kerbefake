package kerbefake.models;

import kerbefake.Utils;
import kerbefake.errors.InvalidMessageException;
import kerbefake.models.EncryptedServerMessageBody;
import kerbefake.models.ServerMessageBody;

import static kerbefake.Logger.error;
import static kerbefake.Utils.assertNonZeroedByteArrayOfLengthN;
import static kerbefake.Utils.byteArrayToLEByteBuffer;

public class Authenticator extends EncryptedServerMessageBody {

    private byte[] iv;

    private byte version;

    private byte[] clientIdBytes;

    private byte[] serverIdBytes;

    private byte[] creationTime;

    @Override
    public Authenticator parse(byte[] bodyBytes) throws Exception {
        if (bodyBytes.length < 48) {
            throw new InvalidMessageException(String.format("Message length is not sufficient (%d but should be at least 48).", bodyBytes.length));
        }
        System.arraycopy(bodyBytes, 0, iv, 0, 16);

    return null;
    }

    @Override
    public byte[] toLEByteArray() {
        if(this.iv == null || this.iv.length != 16){
            throw new RuntimeException("IV is missing or is of invalid length");
        }

        if(this.encryptedData == null || this.encryptedData.length < 64) {
            throw new RuntimeException("Encrypted data is missing or is of invalid length");
        }

        byte[] bytes = new byte[16 + this.encryptedData.length];
        System.arraycopy(this.iv, 0, bytes, 0, 16);
        System.arraycopy(this.encryptedData, 0, bytes, 16, this.encryptedData.length);

        return byteArrayToLEByteBuffer(bytes).array();
    }

    @Override
    public boolean encrypt(byte[] key) {
        if(!assertNonZeroedByteArrayOfLengthN(this.iv, 16)){
            throw new RuntimeException("IV is not initialized or is 0");
        }

        try{
            // 1 - version + 16 - server ID + 16 - client ID + 8 creation time/
            byte[] dataToEncrypt = new byte[1 + 16 + 16 + 8];
            dataToEncrypt[0] = this.version;
            int offset = 1;
            System.arraycopy(this.clientIdBytes, 0, dataToEncrypt, offset, this.clientIdBytes.length);
            offset += this.clientIdBytes.length;
            System.arraycopy(this.serverIdBytes, 0, dataToEncrypt, offset, this.serverIdBytes.length);
            offset += this.serverIdBytes.length;
            System.arraycopy(this.creationTime, 0, dataToEncrypt, offset, this.creationTime.length);

            this.encryptedData = Utils.encrypt(key, this.iv, dataToEncrypt);
            return true;
        } catch (RuntimeException e) {
            error("Encryption failed due to: %s", e);
            return false;
        }
    }

    @Override
    public boolean decrypt(byte[] key) throws InvalidMessageException {
        if(this.encryptedData == null || this.encryptedData.length < 64) {
            throw new RuntimeException("Encrypted data is missing or is of invalid length (at least 64 bytes");
        }

        if(!assertNonZeroedByteArrayOfLengthN(this.iv, 16)){
            throw new RuntimeException("IV is missing or is 0.");
        }
        try{
            byte[] decryptedData = Utils.decrypt(key, this.iv, this.encryptedData);
            if(decryptedData.length != 49){
                throw new InvalidMessageException(String.format("Message length should be 49, got %d", decryptedData.length));
            }

            this.version = decryptedData[0];
            this.clientIdBytes = new byte[16];
            this.serverIdBytes = new byte[16];
            this.creationTime = new byte[8];
            int offset = 1;
            System.arraycopy(decryptedData, offset, this.clientIdBytes, 0, clientIdBytes.length);
            offset += clientIdBytes.length;
            System.arraycopy(decryptedData, offset, this.serverIdBytes, 0, serverIdBytes.length);
            offset += serverIdBytes.length;
            System.arraycopy(decryptedData, offset, this.creationTime, 0, creationTime.length);

            return true;
        } catch (RuntimeException e){
            error("Decryption failed due to: %s", e);
            return false;
        }
    }
}
