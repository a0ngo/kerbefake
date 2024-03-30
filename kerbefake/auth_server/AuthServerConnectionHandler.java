package kerbefake.auth_server;

import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.entities.ServerRequest;
import kerbefake.common.errors.InvalidMessageException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import static kerbefake.common.Logger.error;

/**
 *
 */
public class AuthServerConnectionHandler implements Runnable {

    private final Socket conn;

    private final Thread parentThread;

    public AuthServerConnectionHandler(Socket conn, Thread parentThread) {
        assert conn != null;
        this.conn = conn;
        this.parentThread = parentThread;
    }

    @Override
    public void run() {
        BufferedInputStream in;
        BufferedOutputStream out;
        try {
            in = new BufferedInputStream(conn.getInputStream());
            out = new BufferedOutputStream(conn.getOutputStream());
        } catch (IOException e) {
            error("Failed to created stream reader and writer: %s", e);
            return;
        }

        FailureResponse unknownFailure = new FailureResponse(new ServerMessageHeader((byte) 4, MessageCode.UNKNOWN_FAILURE, 0));
        while (!parentThread.isInterrupted()) {
            try {
                ServerMessage message = ServerMessage.parse(in);
                if (message == null) {
                    continue;
                }
                if (!message.getHeader().getMessageCode().isForAuthServer()) {
                    out.write(unknownFailure.toLEByteArray());
                    continue;
                }

                // On this specific class we always know that we expect messages that are requests, if there's an issue we simply close the connection;
                // Because we do actually allow the parsing of a response message here.
                // This is why we hvae a catch for classcastexception below.
                ServerRequest req = (ServerRequest) message;
                ServerMessage res = req.execute();

                out.write(res.toLEByteArray());
                out.flush();


            } catch (InvalidMessageException | ClassCastException e) {
                // This is just an invalid message, or one we don't know how to handle - ignore and close connection.
                error(e);
                error("Failed to parse message due to: %s", e);
                try {
                    out.write(unknownFailure.toLEByteArray());
                    out.flush();
                    break;
                } catch (IOException ex) {
                    error("Failed to write failure response: %s", e);
                    break;
                }

            } catch (IOException e) {
                error(e);
                error("Failed to send response due to: %s - terminating the connection", e);
                break;

            } catch (Exception e) {
                error("Failed to send response due to: %s", e);
                try {
                    out.write(unknownFailure.toLEByteArray());
                    out.flush();
                } catch (IOException e1) {
                    error(e1);
                    // Hopefully this will be fixed later one.
                }
                break;
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
