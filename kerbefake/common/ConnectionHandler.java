package kerbefake.common;

import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerRequest;
import kerbefake.common.errors.InvalidMessageException;

import java.io.IOException;
import java.net.Socket;

public abstract class ConnectionHandler implements Runnable {

    private final Socket conn;

    private final Thread parentThread;
    
    private final Logger logger;

    /**
     * Which messages this connection accepts.
     */
    private final MessageCode[] acceptedMessages;

    public ConnectionHandler(Socket conn, Thread parentThread, Logger logger, MessageCode[] acceptedMessages) {
        if (conn == null || !conn.isConnected() || conn.isClosed()) {
            throw new RuntimeException("No socket provided or disconnected socket.");
        }
        if (parentThread == null || !parentThread.isAlive() || parentThread.isInterrupted()) {
            throw new RuntimeException("Parent thread is either dead or was interrupted.");
        }
        if(logger == null){
            throw new RuntimeException("No logger provided!");
        }
        if(acceptedMessages == null){
            throw new RuntimeException("Connection handles must specify which messages they accept");
        }
        this.conn = conn;
        this.parentThread = parentThread;
        this.logger = logger;
        this.acceptedMessages = acceptedMessages;
    }

    /**
     * Process the message before we execute the request provided
     *
     * @param message - the message to process before execution.
     * @param <T>     - Some type that is a server message and is a server request that can be executed.
     * @return - an object after processing.
     */
    public abstract <T extends ServerMessage & ServerRequest> T processMessageBeforeExecution(T message);

    @Override
    public void run() {
        MessageStream messageStream;
        try {
            messageStream = new MessageStream(conn, true, parentThread, logger, this.acceptedMessages);
        } catch (IOException e) {
            logger.error("Failed to initialize streams: %s", e.getMessage());
            logger.error(e);
            return;
        }


        FailureResponse unknownFailure = FailureResponse.createUnknownFailureResponse();
        while (!parentThread.isInterrupted()) {
            ServerMessage nextMessage;
            ServerMessage response;

            try {
                nextMessage = messageStream.readNextMessage();
            } catch (IOException | InvalidMessageException e) {
                logger.error(e instanceof IOException ? "Encountered IO Error when reading the next message: %s" : "Failed to read the next message provided due to: %s", e.getMessage());
                logger.error(e);
                boolean sentResponse = messageStream.sendMessage(unknownFailure);
                if (!sentResponse && e instanceof IOException) {
                    // Seems like there's some deeper issue with IO, we can't send the message back.
                    logger.error("Unable to receive and failed to send message, considering socket as broken, closing connection.");
                    break;
                }
                continue;
            } catch (InterruptedException e){
                continue;
            }
            if (nextMessage == null) {
//                debug("No message to read.");
                messageStream.sendMessage(unknownFailure);
                continue;
            }
            if (!ServerRequest.class.isAssignableFrom(nextMessage.getClass())) {
                logger.error("Got a non server request message, can't handle.");
                messageStream.sendMessage(unknownFailure);
                continue;
            }

            try {
                nextMessage = processMessageBeforeExecution((ServerMessage & ServerRequest) nextMessage);
                if (nextMessage == null) {
                    logger.error("Failure processing request before execution, can't proceed.");
                    messageStream.sendMessage(unknownFailure);
                    continue;
                }
                response = ((ServerRequest) nextMessage).execute();
            } catch (InvalidMessageException e) {
                logger.error(e);
                logger.error("Failed to execute server request due to: %s", e.getMessage());
                messageStream.sendMessage(unknownFailure);
                continue;
            }

            if (!messageStream.sendMessage(response)) {
                logger.error("Failed to send message to user.");
            }
        }

        try {
            conn.close();
        } catch (IOException e) {
            logger.error("Failed to close socket due to: %s", e);
            throw new RuntimeException(e);
        }
    }

}
