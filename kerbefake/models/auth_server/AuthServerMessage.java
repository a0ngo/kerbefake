package kerbefake.models.auth_server;

import kerbefake.Constants;
import kerbefake.errors.InvalidMessageException;
import kerbefake.errors.InvalidMessageCodeException;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequest;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequestBody;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponseBody;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static kerbefake.Logger.error;

/**
 * Generic class for auth server message (request or response).
 */
public abstract class AuthServerMessage {

    protected AuthServerMessageHeader header;

    protected AuthServerMessageBody body;

    public AuthServerMessage(AuthServerMessageHeader header, AuthServerMessageBody body) {
        this.header = header;
        this.body = body;
    }

    public AuthServerMessageBody getBody() {
        return body;
    }

    /**
     * Converts the message to a little endian byte array.
     *
     * @return - the little endian byte array.
     */
    public byte[] toLEByteArray() {
        byte[] header = this.header.toLEByteArray();
        byte[] body = this.body == null ? new byte[0] : this.body.toLEByteArray();

        byte[] byteArr = new byte[header.length + body.length];
        System.arraycopy(header, 0, byteArr, 0, header.length);
        System.arraycopy(body, 0, byteArr, header.length, body.length);

        return ByteBuffer.wrap(byteArr).order(ByteOrder.LITTLE_ENDIAN).array();
    }


    public static AuthServerMessage parse(InputStream stream) throws InvalidMessageException {
        BufferedInputStream in = new BufferedInputStream(stream);
        AuthServerMessageHeader header = AuthServerMessage.readHeader(in);
        if (header == null) {
            throw new InvalidMessageException();
        }
        AuthServerMessageBody body = AuthServerMessageBody.parse(header, in);

        MessageCode messageCode = header.getCode();
        try {
            return messageCode.getMessageClazz().getConstructor(AuthServerMessageHeader.class, messageCode.getBodyClazz()).newInstance(header, messageCode.getBodyClazz().cast(body));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            error("Failed to create new message class due to: %s", e);
            throw new InvalidMessageException(String.format("Failed to create a message class due to: %s", e));
        }
    }

    /**
     * Reads the header from the input stream.
     *
     * @param in - the input strema
     * @return An {@link AuthServerMessageHeader} or null in case of an error.
     */
    private static AuthServerMessageHeader readHeader(BufferedInputStream in) {
        try {
            byte[] headerBytes = new byte[Constants.REQUEST_HEADER_SIZE];
            int readBytes = in.read(headerBytes);

            /*
             * -1 is required here because there might be a request with only 23 bytes (just the header, with 0 payload)
             */
            if (readBytes != Constants.REQUEST_HEADER_SIZE && readBytes != -1) {
                error("Failed to read header, expected 23 bytes but got %d", readBytes);
                return null;
            }

            return AuthServerMessageHeader.parseHeader(headerBytes);
        } catch (IOException e) {
            error("Failed to read request header from input stream due to: %s", e);
            return null;
        } catch (InvalidMessageCodeException e) {
            error("%s", e);
            return null;
        }

    }
}
