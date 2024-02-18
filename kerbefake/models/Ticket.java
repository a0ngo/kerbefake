package kerbefake.models;

import kerbefake.Utils;
import kerbefake.errors.InvalidMessageException;

import java.nio.charset.StandardCharsets;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

public class Ticket {

    private byte version;
    private String clientId;

    private String serverId;

    private byte[] creationTime;

    private byte[] ticketIv;

    private byte[] aesKey;

    private byte[] expTime;

    private Ticket(){}

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

    public static Ticket parse(byte[] bytes) throws InvalidMessageException {
        if (bytes.length != 97) {
            throw new InvalidMessageException("Ticket byte size must be 97");
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
        byte[] expTime = byteArrayToLEByteBuffer(bytes, offset, 8).array();

        return new Ticket().setVersion(bytes[0]).setClientId(new String(clientIdBytes, StandardCharsets.UTF_8))
                .setServerId(new String(serverIdBytes, StandardCharsets.UTF_8))
                .setCreationTime(creationTime)
                .setTicketIv(ticketIv)
                .setAesKey(aesKey)
                .setExpTime(expTime);
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
        if (aesKey == null || aesKey.length != 32) {
            throw new RuntimeException("AES Key is missing or invalid");
        }
        if (expTime == null || expTime.length != 8) {
            throw new RuntimeException("Expiration time is missing or invalid");
        }

        byte[] byteArr = new byte[97];
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
        System.arraycopy(aesKey, 0, byteArr, offset, aesKey.length);
        offset += aesKey.length;
        System.arraycopy(expTime, 0, byteArr, offset, expTime.length);

        return byteArrayToLEByteBuffer(byteArr).array();

    }
}
