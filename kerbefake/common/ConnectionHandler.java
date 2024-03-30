package kerbefake.common;

import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerRequest;
import kerbefake.common.errors.InvalidMessageException;

import java.io.IOException;
import java.net.Socket;

import static kerbefake.common.Logger.error;

public abstract class ConnectionHandler implements Runnable {

    private final Socket conn;

    private final Thread parentThread;

    public ConnectionHandler(Socket conn, Thread parentThread) {
        if (conn == null || !conn.isConnected() || conn.isClosed()) {
            throw new RuntimeException("No socket provided or disconnected socket.");
        }
        if (parentThread == null || !parentThread.isAlive() || parentThread.isInterrupted()) {
            throw new RuntimeException("Parent thread is either dead or was interrupted.");
        }
        this.conn = conn;
        this.parentThread = parentThread;
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
            messageStream = new MessageStream(conn, true, parentThread);
        } catch (IOException e) {
            error("Failed to initialize streams: %s", e.getMessage());
            error(e);
            return;
        }


        FailureResponse unknownFailure = FailureResponse.createUnknownFailureResponse();
        while (!parentThread.isInterrupted()) {
            ServerMessage nextMessage;
            ServerMessage response;

            try {
                nextMessage = messageStream.readNextMessage();
            } catch (IOException | InvalidMessageException e) {
                error(e instanceof IOException ? "Encountered IO Error when reading the next message: %s" : "Failed to read the next message provided due to: %s", e.getMessage());
                error(e);
                boolean sentResponse = messageStream.sendMessage(unknownFailure);
                if (!sentResponse && e instanceof IOException) {
                    // Seems like there's some deeper issue with IO, we can't send the message back.
                    error("Unable to receive and failed to send message, considering socket as broken, closing connection.");
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
                error("Got a non server request message, can't handle.");
                messageStream.sendMessage(unknownFailure);
                continue;
            }

            try {
                nextMessage = processMessageBeforeExecution((ServerMessage & ServerRequest) nextMessage);
                if (nextMessage == null) {
                    error("Failure processing request before execution, can't proceed.");
                    messageStream.sendMessage(unknownFailure);
                    continue;
                }
                response = ((ServerRequest) nextMessage).execute();
            } catch (InvalidMessageException e) {
                error(e);
                error("Failed to execute server request due to: %s", e.getMessage());
                messageStream.sendMessage(unknownFailure);
                continue;
            }

            if (!messageStream.sendMessage(response)) {
                error("Failed to send message to user.");
            }
        }

        try {
            conn.close();
        } catch (IOException e) {
            error("Failed to close socket due to: %s", e);
            throw new RuntimeException(e);
        }
    }

}
