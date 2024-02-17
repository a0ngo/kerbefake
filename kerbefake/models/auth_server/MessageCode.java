package kerbefake.models.auth_server;

import kerbefake.Constants;
import kerbefake.errors.InvalidMessageCodeException;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequest;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequestBody;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponseBody;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

public enum MessageCode {

    REGISTER_CLIENT(Constants.RequestCodes.REGISTER_CLIENT_CODE, RegisterClientRequest.class, RegisterClientRequestBody.class),
    REGISTER_CLIENT_SUCCESS(Constants.ResponseCodes.REGISTER_CLIENT_SUCCESS_CODE, RegisterClientResponse.class, RegisterClientResponseBody.class),
    REGISTER_CLIENT_FAILED(Constants.ResponseCodes.REGISTER_CLIENT_FAILURE_CODE, FailureResponse.class, null);

    private final short code;

    private final Class<? extends AuthServerMessage> messageClazz;

    private final Class<? extends AuthServerMessageBody> bodyClazz;

    MessageCode(short code, Class<? extends AuthServerMessage> messageClazz, Class<? extends AuthServerMessageBody> bodyClazz) {
        this.code = code;
        this.messageClazz = messageClazz;
        this.bodyClazz = bodyClazz;
    }

    /**
     * Converts a given request code to a little-endian byte array
     *
     * @return a 2 item byte array for the code in little endian order.
     */
    public byte[] toLEByteArray() {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(this.code).array();
    }


    public short getCode() {
        return this.code;
    }

    public Class<? extends AuthServerMessage> getMessageClazz() {
        return messageClazz;
    }

    public Class<? extends AuthServerMessageBody> getBodyClazz() {
        return bodyClazz;
    }

    /**
     * Parses two bytes as a request code enum
     *
     * @param requestCodeBytes - the request code bytes
     * @return the corresponding {@link MessageCode} for the provided values
     * @throws InvalidMessageCodeException - in case there are more than 2 bytes or it is no a known request code.
     */
    public static MessageCode parse(byte[] requestCodeBytes) throws InvalidMessageCodeException {
        if (requestCodeBytes.length != 2) { // Generally shouldn't happen
            throw new InvalidMessageCodeException("Request Code");
        }
        short reqCode = byteArrayToLEByteBuffer(requestCodeBytes).getShort();

        List<MessageCode> matchingCodes = Arrays.stream(values()).filter(v -> v.code == reqCode).collect(Collectors.toList());
        if (matchingCodes.size() != 1) {
            throw new InvalidMessageCodeException(String.format("Request Code - %d", reqCode));
        }
        return matchingCodes.get(0);
    }

}
