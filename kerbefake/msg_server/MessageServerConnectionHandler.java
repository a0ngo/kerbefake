package kerbefake.msg_server;

import kerbefake.common.entities.*;
import kerbefake.common.errors.CryptographicException;
import kerbefake.common.errors.InvalidHexStringException;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.msg_server.entities.SubmitTicketRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import static kerbefake.common.Logger.error;

public class MessageServerConnectionHandler implements Runnable {

    private Socket conn;

    private byte[] symKey;

    private Thread parentThread;

    public MessageServerConnectionHandler(Socket conn, Thread parentThread, byte[] symKey) {
        this.conn = conn;
        this.symKey = symKey;
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

        while (!this.parentThread.isInterrupted()) {
            try {
                EncryptedServerMessage message = (EncryptedServerMessage) ServerMessage.parse(in);
                if (message == null) {
                    continue;
                }
                if (message.getHeader().getMessageCode().isForAuthServer()) {
                    out.write(unknownFailure.toLEByteArray());
                    continue;
                }
                byte[] key = this.symKey;
                if (!(message instanceof SubmitTicketRequest)) {
                    Ticket sessionTicket = KnownSessions.getInstance().getSession(message.getHeader().getClientID());
                    if (sessionTicket == null) {
                        throw new InvalidMessageException(String.format("Unknown client - no ticket found in memory for %s", message.getHeader().getClientID()));
                    }

                    key = sessionTicket.getAesKey();
                }
                message.decrypt(key);

                ServerMessage response = ((ServerRequest) message).execute();

                out.write(response.toLEByteArray());
                out.flush();
            } catch (InvalidMessageException | CryptographicException | IOException | InvalidHexStringException e) {
                // This is just an invalid message, or one we don't know how to handle - ignore and close connection.
                e.printStackTrace();
                error("Failed to parse message due to: %s", e);
                try {
                    out.write(unknownFailure.toLEByteArray());
                    out.flush();
                    break;
                } catch (IOException | InvalidHexStringException ex) {
                    error("Failed to write failure response: %s", e);
                    break;
                }
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
