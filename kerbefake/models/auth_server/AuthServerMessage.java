package kerbefake.models.auth_server;

import kerbefake.Constants;
import kerbefake.errors.InvalidMessageException;
import kerbefake.errors.InvalidMessageCodeException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
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
        return AuthServerMessage.parse(stream, false);
    }

    public static AuthServerMessage parse(InputStream stream, boolean isResponse) throws InvalidMessageException {
        BufferedInputStream in = new BufferedInputStream(stream);
        AuthServerMessageHeader header = AuthServerMessage.readHeader(in, isResponse);
        if (header == null) {
            throw new InvalidMessageException();
        }
        AuthServerMessageBody body = AuthServerMessageBody.parse(header, in);

        MessageCode messageCode = header.getCode();

        /*
         * Here we parse a message according to the specified message class in MessageCode.
         * We expect there to be a constructor of signature (AuthServerMessageHeader header) or
         * (AuthServerMessageHeader header, AuthServerMessageBody body).
         */
        try {
            Class<? extends AuthServerMessage> messageClass = messageCode.getMessageClass();
            Class<? extends AuthServerMessageBody> bodyClass = messageCode.getBodyClass();
            if (bodyClass == null) {
                return messageClass.getConstructor(AuthServerMessageHeader.class).newInstance(header);
            }

            return messageClass.getConstructor(AuthServerMessageHeader.class, messageCode.getBodyClass())
                    .newInstance(header, messageCode.getBodyClass().cast(body));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            // See above note if you face this error.
            error("Failed to create new message class due to: %s", e);
            throw new InvalidMessageException(String.format("Failed to create a message class due to: %s", e));
        }
    }

    /**
     * Reads the header from the input stream.
     *
     * @param in - the input stream
     * @return An {@link AuthServerMessageHeader} or null in case of an error.
     */
    private static AuthServerMessageHeader readHeader(BufferedInputStream in, boolean isResponse) {
        try {
            byte[] headerBytes = new byte[isResponse ? Constants.RESPONSE_HEADER_SIZE : Constants.REQUEST_HEADER_SIZE];
            int readBytes = in.read(headerBytes);

            /*
             * -1 is required here because there might be a request with only 23 bytes (just the header, with 0 payload)
             */
            if (readBytes != headerBytes.length && readBytes != -1) {
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
