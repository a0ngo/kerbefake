package kerbefake.models;

import kerbefake.Utils;
import kerbefake.errors.InvalidMessageException;
import kerbefake.models.auth_server.MessageField;

import java.nio.charset.StandardCharsets;

import static kerbefake.Logger.error;
import static kerbefake.Utils.assertNonZeroedByteArrayOfLengthN;
import static kerbefake.Utils.byteArrayToLEByteBuffer;

public class Ticket implements MessageField {

    public static final int SIZE = 105;

    private byte version;
    private String clientId;

    private String serverId;

    private byte[] creationTime;

    private byte[] ticketIv;

    private byte[] aesKey;

    private byte[] encAesKey;

    private byte[] expTime;

    private byte[] encExpTime;

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

    private Ticket setEncAesKey(byte[] encAesKey) {
        this.encAesKey = encAesKey;
        return this;
    }

    private Ticket setEncExpTime(byte[] encExpTime) {
        this.encExpTime = encExpTime;
        return this;
    }

    public boolean decrypt(byte[] key) {
        if (!assertNonZeroedByteArrayOfLengthN(this.ticketIv, 16)) {
            return false;
        }
        try {
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
            error("Missing nonce or aes key for encryption.");
            return false;
        }

        if (!assertNonZeroedByteArrayOfLengthN(this.expTime, 8)) {
            error("Expiration time is zeroed out");
            return false;
        }

        // TODO: Do we allow a zeroed AES? it might imply a lack of initialization
        try {
            setEncExpTime(Utils.encrypt(key, ticketIv, this.expTime));
            setEncAesKey(Utils.decrypt(key, ticketIv, this.aesKey));
            return true;
        } catch (RuntimeException e) {
            return false;
        }

    }


    public static Ticket parse(byte[] bytes) throws InvalidMessageException {
        /*
        Parsing the ticket assumes that the fields aesKey and expTime are encrypted.
        As a result their size is not the same as the size specified in the protocol since expTime is 8 bytes but
        encrypted it'll be 16.
         */

        if (bytes.length != SIZE) {
            throw new InvalidMessageException("Ticket byte size must be 105");
        }

        int offset = 1;
        byte[] clientIdBytes = byteArrayToLEByteBuffer(bytes, offset, 16).array();
        offset += 16;
        byte[] serverIdBytes = byteArrayToLEByteBuffer(bytes, offset, 16).array();
        offset += 16;
        byte[] creationTime = byteArrayToLEByteBuffer(bytes, offset, 8).array();
        offset += 8;
        byte[] ticketIv = byteArrayToLEByteBuffer(bytes, offset, 16).array();
        offset += 16;
        byte[] aesKey = byteArrayToLEByteBuffer(bytes, offset, 32).array();
        offset += 32;
        byte[] expTime = byteArrayToLEByteBuffer(bytes, offset, 16).array();

        return new Ticket().setVersion(bytes[0]).setClientId(new String(clientIdBytes, StandardCharsets.UTF_8))
                .setServerId(new String(serverIdBytes, StandardCharsets.UTF_8))
                .setCreationTime(creationTime)
                .setTicketIv(ticketIv)
                .setEncAesKey(aesKey)
                .setEncExpTime(expTime);
    }

    public byte[] toLEByteArray() {
        if (clientId == null || clientId.length() != 16) {
            throw new RuntimeException("Client id is missing or invalid");
        }
        if (serverId == null || serverId.length() != 16) {
            throw new RuntimeException("Server id is missing or invalid");
        }
        if (creationTime == null || creationTime.length != 8) {
            throw new RuntimeException("Creation time is missing or invalid");
        }
        if (ticketIv == null || ticketIv.length != 16) {
            throw new RuntimeException("Ticket IV is missing or invalid");
        }
        if (encAesKey == null || encAesKey.length != 32) {
            throw new RuntimeException("AES Key is missing or invalid - make sure to run encrypt before converting to LE Byte array");
        }
        if (encExpTime == null || encExpTime.length != 16) {
            throw new RuntimeException("Expiration time is missing or invalid - make sure to run encrypt before converting to LE Byte array");
        }

        byte[] byteArr = new byte[SIZE];
        byteArr[0] = version;
        int offset = 1;

        System.arraycopy(clientId.getBytes(), 0, byteArr, offset, clientId.length());
        offset += clientId.length();
        System.arraycopy(serverId.getBytes(), 0, byteArr, offset, serverId.length());
        offset += serverId.length();
        System.arraycopy(creationTime, 0, byteArr, offset, creationTime.length);
        offset += creationTime.length;
        System.arraycopy(ticketIv, 0, byteArr, offset, ticketIv.length);
        offset += ticketIv.length;
        System.arraycopy(encAesKey, 0, byteArr, offset, encAesKey.length);
        offset += encAesKey.length;
        System.arraycopy(expTime, 0, byteArr, offset, encExpTime.length);

        return byteArrayToLEByteBuffer(byteArr).array();

    }
}
