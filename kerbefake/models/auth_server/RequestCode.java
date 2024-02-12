package kerbefake.models.auth_server;

import kerbefake.Constants;
import kerbefake.errors.InvalidRequestException;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

public enum RequestCode {

    REGISTER_CLIENT(Constants.RequestCodes.REGISTER_CLIENT_CODE);

    private final short code;

    RequestCode(short code) {
        this.code = code;
    }

    /**
     * Parses two bytes as a request code enum
     *
     * @param requestCodeBytes - the request code bytes
     * @return the corresponding {@link RequestCode} for the provided values
     * @throws InvalidRequestException - in case there are more than 2 bytes or it is no a known request code.
     */
    public static RequestCode parse(byte[] requestCodeBytes) throws InvalidRequestException {
        if (requestCodeBytes.length != 2) { // Generally shouldn't happen
            throw new InvalidRequestException("Request Code");
        }
        short reqCode = byteArrayToLEByteBuffer(requestCodeBytes).getShort();
        switch (reqCode) {
            case Constants.RequestCodes.REGISTER_CLIENT_CODE:
                return REGISTER_CLIENT;
            default:
                throw new InvalidRequestException(String.format("Request Code - %d", reqCode));
        }
    }

}
