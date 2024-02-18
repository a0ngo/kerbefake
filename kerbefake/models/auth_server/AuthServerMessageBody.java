package kerbefake.models.auth_server;

import kerbefake.errors.InvalidMessageException;
import kerbefake.errors.InvalidMessageCodeException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static kerbefake.Logger.error;

public abstract class AuthServerMessageBody {


    public AuthServerMessageBody() {
    }

    /**
     * Parses a specific message from a given array of LE bytes.
     *
     * @param bodyBytes - the body bytes that were read
     * @return - a child of {@link AuthServerMessageBody}
     * @throws Exception - in case of a request parsing it might throw {@link InvalidMessageCodeException}, in case of response parsing it might throw {@link kerbefake.errors.InvalidResponseDataException}
     */
    public abstract AuthServerMessageBody parse(byte[] bodyBytes) throws Exception;

    /**
     * Converts this body to a LE byte array.
     *
     * @return - the LE byte array representing this response.
     */
    public abstract byte[] toLEByteArray();

    public static AuthServerMessageBody parse(AuthServerMessageHeader header, BufferedInputStream stream) throws InvalidMessageException {
        int payloadSize = header.getPayloadSize();
        if (payloadSize == 0) {
            return null;
        }
        byte[] bodyBytes = new byte[payloadSize];

        try {
            int readBytes = stream.read(bodyBytes);
            if (readBytes != payloadSize && readBytes != -1) {
                error("Failed to read body, expected %d bytes, but got %d", readBytes);
                return null;
            }

            try {
                return header.getCode().getBodyClass().getConstructor().newInstance().parse(bodyBytes);
            } catch (InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                error("Failed to create new message class (please make sure the body has an empty constructor and the parse function!) due to: %s", e);
                throw new InvalidMessageException(header.getCode());
            }

        } catch (IOException | InvocationTargetException e) {
            error("Failed to read request body from input stream due to: %s", e);
            return null;
        } catch (Exception e) {
            error("%s", e);
            return null;
        }

    }

}
