package kerbefake.msg_server;

import kerbefake.common.ConnectionHandler;
import kerbefake.common.entities.EncryptedServerMessage;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerRequest;
import kerbefake.common.entities.Ticket;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.msg_server.entities.SubmitTicketRequest;

import java.net.Socket;

import static kerbefake.common.Logger.error;

public class MessageServerConnectionHandler extends ConnectionHandler {

    private final byte[] symKey;

    public MessageServerConnectionHandler(Socket conn, Thread parentThread, byte[] symKey) {
        super(conn, parentThread);
        this.symKey = symKey;
    }

    @Override
    public <T extends ServerMessage & ServerRequest> T processMessageBeforeExecution(T message) {
        byte[] key = this.symKey;
        if (!(message instanceof SubmitTicketRequest)) {
            Ticket sessionTicket = KnownSessions.getInstance().getSession(message.getHeader().getClientID());
            if (sessionTicket == null) {
                error("Unknown client - no ticket found in memory for %s", message.getHeader().getClientID());
                return null;
            }

            key = sessionTicket.getAesKey();
        }
        try {
            ((EncryptedServerMessage) message).decrypt(key);
        } catch (InvalidMessageException e) {
            error("Failed to decrypt server message due to: %s", e.getMessage());
            error(e);
            return null;
        }
        return message;
    }
}
