package kerbefake.common.entities;

import kerbefake.auth_server.errors.InvalidResponseDataException;
import kerbefake.common.errors.InvalidMessageCodeException;
import kerbefake.common.errors.InvalidMessageException;

public abstract class ServerMessageBody {


    public ServerMessageBody() {
    }

    /**
     * Parses a specific message from a given array of LE bytes.
     *
     * @param bodyBytes - the body bytes that were read
     * @return - a child of {@link ServerMessageBody}
     * @throws Exception - in case of a request parsing it might throw {@link InvalidMessageCodeException}, in case of response parsing it might throw {@link InvalidResponseDataException}
     */
    public abstract ServerMessageBody parse(byte[] bodyBytes) throws Exception;

    /**
     * Converts this body to a LE byte array.
     *
     * @return - the LE byte array representing this response.
     * @throws InvalidMessageException - in case the message data is invalid or there was some problem in the serialization of the message
     */
    public abstract byte[] toLEByteArray() throws InvalidMessageException;
//
//    public static ServerMessageBody parse(ServerMessageHeader header, BufferedInputStream stream) throws InvalidMessageException {
//        int payloadSize = header.getPayloadSize();
//        if (payloadSize == 0) {
//            return null;
//        }
//        byte[] bodyBytes = new byte[payloadSize];
//
//        try {
//            int readBytes = stream.read(bodyBytes);
//            if (readBytes != payloadSize && readBytes != -1) {
//                error("Failed to read body, expected %d bytes, but got %d", readBytes);
//                return null;
//            }
//
//            try {
//                return header.getMessageCode().getBodyClass().getConstructor().newInstance().parse(byteArrayToLEByteBuffer(bodyBytes).array());
//            } catch (InstantiationException | IllegalAccessException |
//                     NoSuchMethodException e) {
//                error("Failed to create new message class (please make sure the body has an empty constructor and the parse function!) due to: %s", e);
//                throw new InvalidMessageException(header.getMessageCode());
//            }
//
//        } catch (IOException | InvocationTargetException e) {
//            error(e);
//            error("Failed to read request body from input stream due to: %s", e);
//            return null;
//        } catch (Exception e) {
//            error(e);
//            error("%s", e);
//            return null;
//        }
//
//    }

}
