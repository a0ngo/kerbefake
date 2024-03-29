package kerbefake.models;

import kerbefake.Utils;
import kerbefake.errors.InvalidHexStringException;
import kerbefake.errors.InvalidMessageException;

import java.nio.charset.StandardCharsets;

import static kerbefake.Constants.ID_LENGTH;
import static kerbefake.Logger.error;
import static kerbefake.Logger.info;
import static kerbefake.Utils.*;

public class Ticket extends EncryptedServerMessageBody {

    public static final int DATA_ENCRYPTED_SIZE = 48;

    public static final int DATA_DECRYPTED_SIZE = 40;

    private byte version;
    private String clientId;

    private String serverId;

    private byte[] creationTime;

    private byte[] ticketIv;

    private byte[] aesKey;

    private byte[] expTime;

    public Ticket() {
    }

    public Ticket setVersion(byte version) {
        this.version = version;
        return this;
    }

    public Ticket setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public Ticket setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public Ticket setCreationTime(byte[] creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public Ticket setTicketIv(byte[] ticketIv) {
        this.ticketIv = ticketIv;
        return this;
    }

    public Ticket setAesKey(byte[] aesKey) {
        this.aesKey = aesKey;
        return this;
    }

    public Ticket setExpTime(byte[] expTime) {
        this.expTime = expTime;
        return this;
    }

    public byte[] getAesKey() {
        return aesKey;
    }

    @Override
    public boolean decrypt(byte[] key) throws InvalidMessageException {
        if (!assertNonZeroedByteArrayOfLengthN(this.ticketIv, 16)) {
            return false;
        }
        try {
            if (!assertNonZeroedByteArrayOfLengthN(this.encryptedData, DATA_ENCRYPTED_SIZE)) {
                throw new RuntimeException("No encrypted data or data is of invalid size.");
            }

            byte[] decryptedData = Utils.decrypt(key, this.ticketIv, this.encryptedData);
            if (decryptedData.length != DATA_DECRYPTED_SIZE) {
                error("Invalid decryption size, expected %d got %d", DATA_DECRYPTED_SIZE, decryptedData.length);
                return false;
            }

            this.aesKey = new byte[32];
            this.expTime = new byte[8];

            System.arraycopy(decryptedData, 0, aesKey, 0, 32);
            System.arraycopy(decryptedData, 32, expTime, 0, 8);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean encrypt(byte[] key) {
        if (!assertNonZeroedByteArrayOfLengthN(this.ticketIv, 16)) {
            return false;
        }
        if (this.expTime == null || this.aesKey == null || this.expTime.length != 8 || this.aesKey.length != 32) {
            error("Missing exp time or aes key for encryption.");
            return false;
        }

        if (!assertNonZeroedByteArrayOfLengthN(this.expTime, 8)) {
            error("Expiration time is zeroed out");
            return false;
        }
        try {
            byte[] dataToEncrypt = new byte[DATA_DECRYPTED_SIZE];
            System.arraycopy(aesKey, 0, dataToEncrypt, 0, 32);
            System.arraycopy(expTime, 0, dataToEncrypt, 32, 8);

            this.encryptedData = Utils.encrypt(key, this.ticketIv, dataToEncrypt);
            info("TEST - Decrypted data: %s, Encrypted: %s", bytesToHexString(dataToEncrypt), bytesToHexString(encryptedData));
            return true;
        } catch (RuntimeException e) {
            return false;
        }

    }


    @Override
    public Ticket parse(byte[] bytes) throws InvalidMessageException {
        // We do not enforce length before decryption
        int offset = 1;
        byte[] clientIdBytes = byteArrayToLEByteBuffer(bytes, offset, 16).array();
        offset += 16;
        byte[] serverIdBytes = byteArrayToLEByteBuffer(bytes, offset, 16).array();
        offset += 16;
        byte[] creationTime = byteArrayToLEByteBuffer(bytes, offset, 8).array();
        offset += 8;
        byte[] ticketIv = byteArrayToLEByteBuffer(bytes, offset, 16).array();
        offset += 16;
        byte[] encryptedTicket = byteArrayToLEByteBuffer(bytes, offset, bytes.length - offset).array();

        Ticket ticket = new Ticket().setVersion(bytes[0]).setClientId(bytesToHexString(clientIdBytes))
                .setServerId(bytesToHexString(serverIdBytes))
                .setCreationTime(creationTime)
                .setTicketIv(ticketIv);
        ticket.encryptedData = encryptedTicket;
        return ticket;
    }


    public byte[] toLEByteArray() throws InvalidHexStringException {
        if (clientId == null || clientId.length() != ID_LENGTH) {
            throw new RuntimeException("Client id is missing or invalid");
        }
        if (serverId == null || serverId.length() != ID_LENGTH) {
            throw new RuntimeException("Server id is missing or invalid");
        }
        if (creationTime == null || creationTime.length != 8) {
            throw new RuntimeException("Creation time is missing or invalid");
        }
        if (ticketIv == null || ticketIv.length != 16) {
            throw new RuntimeException("Ticket IV is missing or invalid");
        }

        if (encryptedData == null || encryptedData.length < 48) { // 32 aes + 8 exp time at least 48 bytes encrypted
            throw new RuntimeException("Object must be encrypted before sending");
        }

        byte[] clientIdBytes = hexStringToByteArray(clientId);
        byte[] serverIdBytes = hexStringToByteArray(serverId);
        int objectSize = clientIdBytes.length + serverIdBytes.length + 1 /*Version*/ + creationTime.length + 16 /*IV*/ + this.encryptedData.length;
        byte[] byteArr = new byte[objectSize];
        byteArr[0] = version;
        int offset = 1;

        System.arraycopy(clientIdBytes, 0, byteArr, offset, clientIdBytes.length);
        offset += clientIdBytes.length;
        System.arraycopy(serverIdBytes, 0, byteArr, offset, serverIdBytes.length);
        offset += serverIdBytes.length;
        System.arraycopy(creationTime, 0, byteArr, offset, creationTime.length);
        offset += creationTime.length;
        System.arraycopy(ticketIv, 0, byteArr, offset, ticketIv.length);
        offset += ticketIv.length;
        System.arraycopy(encryptedData, 0, byteArr, offset, encryptedData.length);

        return byteArrayToLEByteBuffer(byteArr).array();

    }

    public boolean isEncrypted() {
        return !assertNonZeroedByteArrayOfLengthN(this.aesKey, 32) || !assertNonZeroedByteArrayOfLengthN(this.expTime, 8);
    }

    public byte[] getExpTime() {
        return expTime;
    }

    public byte[] getCreationTime() {
        return creationTime;
    }
}
