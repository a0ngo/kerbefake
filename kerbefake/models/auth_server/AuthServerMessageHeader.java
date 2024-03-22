package kerbefake.models.auth_server;

import kerbefake.Constants;
import kerbefake.errors.InvalidMessageCodeException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static kerbefake.Utils.*;

/**
 * A model representing the header in the response or request to the authentication server
 * The header has a fixed size of 23 bytes.
 * Client ID - 16 bytes
 * Version - 1 byte
 * Code - 2 bytes
 * Payload Size - 4 bytes
 */
public class AuthServerMessageHeader implements Message {

    private byte[] rawHeader;

    private String clientID;

    private byte version;
    private MessageCode code;

    private int payloadSize;

    public AuthServerMessageHeader(byte version, MessageCode code, int payloadSize) {
        this(null, version, code, payloadSize);
    }


    public AuthServerMessageHeader(String clientID, byte version, MessageCode code, int payloadSize) {
        this.clientID = clientID;
        this.version = version;
        this.code = code;
        this.payloadSize = payloadSize;
    }


    private void setRawHeader(byte[] rawHeader) {
        this.rawHeader = rawHeader;
    }

    public byte[] getRawHeader() {
        return rawHeader;
    }

    public String getClientID() {
        return clientID;
    }

    public MessageCode getCode() {
        return code;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    /**
     * Clones this header and returns one with altered message code and payload size
     *
     * @param newCode     - the new code to use
     * @param payloadSize - the  new payload size to use
     * @return the new header.
     */
    public AuthServerMessageHeader toResponseHeader(MessageCode newCode, int payloadSize) {
        return new AuthServerMessageHeader(this.version, newCode, payloadSize);
    }

    /**
     * Parses a byte array as the request header.
     *
     * @param rawHeader - the raw bytes for the header
     * @return the {@link AuthServerMessageHeader} for the provided bytes
     * @throws InvalidMessageCodeException - In case the data provided is invalid.
     */
    public static AuthServerMessageHeader parseHeader(byte[] rawHeader) throws InvalidMessageCodeException {
        if (rawHeader == null || (rawHeader.length != Constants.REQUEST_HEADER_SIZE && rawHeader.length != Constants.RESPONSE_HEADER_SIZE)) {
            throw new InvalidMessageCodeException("header");
        }

        int offset = 0;
        String clientId = null;
        if (rawHeader.length == Constants.REQUEST_HEADER_SIZE) {
            clientId = bytesToHexString(byteArrayToLEByteBuffer(rawHeader, 0, 16).array());
            offset += 16;
        }
        byte version = rawHeader[offset++];
        byte[] reqCodeBytes = {rawHeader[offset++], rawHeader[offset++]};
        MessageCode reqCode = MessageCode.parse(reqCodeBytes);
        int payloadSize = byteArrayToLEByteBuffer(rawHeader, offset, 4).getInt();

        if (payloadSize < 0) {
            throw new InvalidMessageCodeException("Payload Size");
        }
        if (version < 0) {
            throw new InvalidMessageCodeException("Version");
        }

        AuthServerMessageHeader header = new AuthServerMessageHeader(clientId, version, reqCode, payloadSize);
        header.setRawHeader(rawHeader);

        return header;
    }

    @Override
    public byte[] toLEByteArray() {
        // The raw header is received in little endian so we can return it.
        if (this.rawHeader != null) {
            return this.rawHeader;
        }

        // Byte array size is constant
        byte[] byteArr = new byte[this.clientID == null ? Constants.RESPONSE_HEADER_SIZE : Constants.REQUEST_HEADER_SIZE];
        byte[] versionByteArray = new byte[]{this.version};
        byte[] messageCode = this.code.toLEByteArray();
        byte[] payloadSize = intToLEByteArray(this.payloadSize);
        int offsetCounter = 0;

        if (this.clientID != null) {
            byte[] idByteArr = hexStringToByteArray(this.clientID);
            System.arraycopy(idByteArr, 0, byteArr, offsetCounter, idByteArr.length);
            offsetCounter += idByteArr.length;
        }
        System.arraycopy(versionByteArray, 0, byteArr, (offsetCounter++), 1);
        System.arraycopy(messageCode, 0, byteArr, offsetCounter, 2);
        offsetCounter += 2;
        System.arraycopy(payloadSize, 0, byteArr, offsetCounter, 4);

        this.rawHeader = ByteBuffer.allocate(byteArr.length).order(ByteOrder.LITTLE_ENDIAN).put(byteArr).array();
        return this.toLEByteArray();
    }
}
