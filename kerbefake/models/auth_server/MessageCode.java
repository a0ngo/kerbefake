package kerbefake.models.auth_server;

import kerbefake.Constants;
import kerbefake.errors.InvalidMessageCodeException;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequest;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequestBody;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponseBody;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

/**
 * An enum of all available messages to use or to parse.
 */
public enum MessageCode {

    /**
     * A request to register a client
     */
    REGISTER_CLIENT(Constants.RequestCodes.REGISTER_CLIENT_CODE, RegisterClientRequest.class, RegisterClientRequestBody.class),

    /**
     * Successful response for client registration.
     */
    REGISTER_CLIENT_SUCCESS(Constants.ResponseCodes.REGISTER_CLIENT_SUCCESS_CODE, RegisterClientResponse.class, RegisterClientResponseBody.class),
    /**
     * Failure response for client registration.
     */
    REGISTER_CLIENT_FAILED(Constants.ResponseCodes.REGISTER_CLIENT_FAILURE_CODE, FailureResponse.class, null);

    /**
     * The code for a given request
     */
    private final short code;

    /**
     * The class of the message that is used for the given message.
     * This is used when parsing the message from the input stream in {@link kerbefake.AuthServerConnectionHandler}
     * or as part of a message parsing in {@link AuthServerMessage#parse(InputStream)}.
     * The class must extend {@link AuthServerMessage} and thus have a constructor matching the signature:
     * `{@link AuthServerMessageHeader}, {@link AuthServerMessageBody}`
     */
    private final Class<? extends AuthServerMessage> messageClazz;

    /**
     * The class of the message's body. This class can be null, for example a failure response has no body, just the header.
     * If this exists it must extend {@link AuthServerMessageBody} and implement an empty constructor with the {@link AuthServerMessageBody#parse(byte[])}
     * method.
     */
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

    public Class<? extends AuthServerMessage> getMessageClass() {
        return messageClazz;
    }

    public Class<? extends AuthServerMessageBody> getBodyClass() {
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
