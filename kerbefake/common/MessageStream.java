package kerbefake.common;

import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageBody;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.errors.InvalidMessageCodeException;
import kerbefake.common.errors.InvalidMessageException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import static kerbefake.common.Constants.RESPONSE_HEADER_SIZE;
import static kerbefake.common.Utils.byteArrayToLEByteBuffer;

/**
 * This class is meant to wrap around the {@link java.io.InputStream} and {@link java.io.OutputStream} and provide
 * a simple interface to use when reading and writing messages to the socket's streams.
 */
public final class MessageStream {

    private final InputStream inputStream;

    private final OutputStream outputStream;

    private final int HEADER_SIZE;

    private final Thread parentThread;

    private final Logger logger;

    /**
     * Which messages this server accepts.
     */
    private MessageCode[] acceptedMessages;

    public MessageStream(Socket connectionSocket, boolean isServer, Thread parentThread, Logger logger) throws IOException {
        this(connectionSocket, isServer, parentThread, logger, null);
    }

    /**
     * Creates a new MessageStream.
     *
     * @param connectionSocket - the socket used for communication.
     * @param isServer         - whether whoever is creating this stream is a server, if it is behavior is slightly different
     * @throws IOException - in case of a problem getting the streams from the socket.
     */
    public MessageStream(Socket connectionSocket, boolean isServer, Thread parentThread, Logger logger, MessageCode[] acceptedMessages) throws IOException {
        this.inputStream = connectionSocket.getInputStream();
        this.outputStream = connectionSocket.getOutputStream();
        this.HEADER_SIZE = isServer ? Constants.REQUEST_HEADER_SIZE : RESPONSE_HEADER_SIZE;
        this.parentThread = parentThread;
        this.logger = logger;
        this.acceptedMessages = acceptedMessages;
    }

    /**
     * A blocking call that will read from the socket's underlying stream to get the next message.
     * We first examine the state of the stream to see if we have some data to read.
     * If we do, we check how many bytes. If we have x>23 bytes waiting we are good to go and start reading.
     * If we have 0<x<=9 bytes waiting, we wait for 100 more reads in case some data is still on the way.
     * <p>
     * Once done, we read the header data.
     * We know if the data is a response by the first byte - if it's a 24 decimal and the next two bytes are one of the possible codes, it's a response,
     *
     * @return - A {@link ServerMessage} that was read from the stream.
     */
    public ServerMessage readNextMessage() throws InvalidMessageException, IOException, InterruptedException {
        ServerMessageHeader messageHeader;
        ServerMessageBody messageBody;

        while (!parentThread.isInterrupted()) {
            if (inputStream.available() > 0) {
                break;
            }
        }
        if (parentThread.isInterrupted()) {
            throw new InterruptedException();
        }

        // Read message header
        byte[] headerBytes = new byte[HEADER_SIZE];
        int readBytes = inputStream.read(headerBytes);

        // No data is waiting on stream, stopping.
        if (readBytes == -1) {
//            logger.debug("No bytes waiting on stream.");
            return null;
        }

        if (readBytes != HEADER_SIZE) {
            logger.error("Failed to read header, expected 23 bytes but got %d", readBytes);
            throw new InvalidMessageException(String.format("Failed to read header, expected 23 bytes but got %d", readBytes));
        }

        try {
            messageHeader = ServerMessageHeader.parseHeader(byteArrayToLEByteBuffer(headerBytes).array());
        } catch (InvalidMessageCodeException e) {
            logger.error(e);
            throw new InvalidMessageException("Invalid message code provided.");
        }

        boolean acceptMessage = acceptedMessages == null;
        if (acceptedMessages != null)
            for (MessageCode code : acceptedMessages) {
                acceptMessage |= messageHeader.getMessageCode().getCode() == code.getCode();
            }

        if (!acceptMessage) {
            // Read all remaining data to clear the socket before exiting.
            if(messageHeader.getPayloadSize() > 0) {
                byte[] garbage = new byte[ messageHeader.getPayloadSize()];
                int ignored = inputStream.read(garbage);
            }
            logger.info("Received a message that should not accept: %s", messageHeader.getMessageCode().getMessageClass().getCanonicalName());
            return null;
        }
        // Now we read the body if one exists
        int payloadSize = messageHeader.getPayloadSize();
        byte[] bodyBytes = new byte[payloadSize];
        if (payloadSize != 0) {
            logger.debug("Reading payload for %d bytes", payloadSize);
            readBytes = inputStream.read(bodyBytes);
            if (readBytes != payloadSize) {
                logger.error("Failed to read body, expected %d bytes, but got %d", payloadSize, readBytes);
                return null;
            }
        }

        MessageCode messageCode = messageHeader.getMessageCode();
        Class<? extends ServerMessage> messageClass = messageCode.getMessageClass();
        Class<? extends ServerMessageBody> bodyClass = messageCode.getBodyClass();
        logger.debug("Trying to parse message body for code: %d ", messageCode.getCode());

        /*
         * Here we finish building a message according to the specified message class in MessageCode.
         * We expect there to be a constructor of signature (ServerMessageHeader) or
         * (ServerMessageHeader header, ServerMessageBody body).
         */
        try {
            if (bodyClass != null && payloadSize > 0) {
                messageBody = bodyClass.getConstructor().newInstance().parse(byteArrayToLEByteBuffer(bodyBytes).array());
                return messageClass.getConstructor(ServerMessageHeader.class, messageCode.getBodyClass()).newInstance(messageHeader, messageCode.getBodyClass().cast(messageBody));
            } else if (bodyClass != null) {
                logger.error("Provided body type however no payload provided as part of the message.");
                return null;
            }
            return messageClass.getConstructor(ServerMessageHeader.class).newInstance(messageHeader);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            logger.errorToFileOnly("Failed to create new message class (please make sure the body has an empty constructor and the parse function!) due to: %s", e);
            logger.error(e);
            throw new InvalidMessageException(messageCode);

        } catch (IOException | InvocationTargetException e) {
            logger.error("Failed to read request body from input stream due to: %s", e);
            logger.error(e);
            return null;
        } catch (Exception e) {
            logger.error("Unknown error occurred: %s", e.getMessage());
            logger.error(e);
            return null;
        }
    }

    /**
     * Sends a message over the stream.
     *
     * @param message - the message to send
     * @return - true if it was successfully sent, false otherwise.
     */
    public boolean sendMessage(ServerMessage message) {
        try {
            outputStream.write(message.toLEByteArray());
            return true;
        } catch (InvalidMessageException e) {
            logger.error(e);
            logger.error(e.getMessage());
        } catch (IOException e) {
//            logger.error("Failed to send message due to: %s", e.getMessage());
            logger.error(e);
        }
        return false;
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
    }
}
