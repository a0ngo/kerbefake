package kerbefake.models.auth_server;

import kerbefake.Constants;
import kerbefake.errors.InvalidRequestException;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

/**
 * A model representing the header in a request to the authentication server
 * The header has a fixed size of 23 bytes.
 * Client ID - 16 bytes
 * Version - 1 byte
 * Code - 2 bytes
 * Payload Size - 4 bytes
 */
public class AuthServerRequestHeader {

    private byte[] rawHeader;

    private String clientID;

    private byte version;
    private RequestCode code;

    private int payloadSize;

    public AuthServerRequestHeader(String clientID, byte version, RequestCode code, int payloadSize) {
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

    public RequestCode getCode() {
        return code;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    /**
     * Parses a byte array as the request header.
     *
     * @param rawHeader - the raw bytes for the header
     * @return the {@link AuthServerRequestHeader} for the provided bytes
     * @throws InvalidRequestException - In case the data provided is invalid.
     */
    public static AuthServerRequestHeader parseHeader(byte[] rawHeader) throws InvalidRequestException {
        if (rawHeader == null || rawHeader.length != Constants.REQUEST_HEADER_SIZE) {
            throw new InvalidRequestException("header");
        }

        String clientId = byteArrayToLEByteBuffer(rawHeader, 0, 16).toString();
        byte version = rawHeader[16];
        byte[] reqCodeBytes = {rawHeader[17], rawHeader[18]};
        RequestCode reqCode = RequestCode.parse(reqCodeBytes);
        int payloadSize = byteArrayToLEByteBuffer(rawHeader, 19, 4).getInt();

        if(payloadSize < 0){
            throw new InvalidRequestException("Payload Size");
        }
        if(version < 0){
            throw new InvalidRequestException("Version");
        }

        AuthServerRequestHeader header = new AuthServerRequestHeader(clientId, version, reqCode, payloadSize);
        header.setRawHeader(rawHeader);

        return header;
    }
}
