package kerbefake.models.msg_server.requests;

import kerbefake.errors.InvalidMessageException;
import kerbefake.models.*;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.EmptyResponse;
import kerbefake.msg_server.KnownSessions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static kerbefake.Logger.error;
import static kerbefake.Logger.info;

public class SubmitTicketRequest extends EncryptedServerMessage implements ServerRequest {

    public SubmitTicketRequest(ServerMessageHeader header, SubmitTicketRequestBody body) {
        super(header, body);
    }


    @Override
    public ServerMessage execute() {
        FailureResponse failedResponse = new FailureResponse(this.header.toResponseHeader(MessageCode.UNKNOWN_FAILURE, 0));

        if (this.body == null) {
            error("No body provided for submit ticket request.");
            return failedResponse;
        }

        KnownSessions sessions = KnownSessions.getInstance();

        SubmitTicketRequestBody body = (SubmitTicketRequestBody) this.body;
        Ticket ticket = body.getTicket();
        if (ticket.isEncrypted()) {
            error("Ticket was not decrypted ahead of execution, ignoring.");
            return failedResponse;
        }

        long expTime = ByteBuffer.wrap(ticket.getExpTime()).order(ByteOrder.LITTLE_ENDIAN).getLong();
        info("Ticket expired, current time: %d, exp time: %d", System.currentTimeMillis(), expTime);
        if (System.currentTimeMillis() >= expTime) {
            return failedResponse;
        }

        sessions.addSession(header.getClientID(), ticket);
        return new EmptyResponse(this.header.toResponseHeader(MessageCode.SUBMIT_TICKET_SUCCESS, 0));
    }

    @Override
    public void encrypt(byte[] key) {
        if (this.body == null) {
            throw new RuntimeException("No body for the message, nothing to encrypt");
        }
        SubmitTicketRequestBody body = (SubmitTicketRequestBody) this.body;
        if (!body.getTicket().encrypt(key)) {
            throw new RuntimeException("Unable to encrypt message");
        }

        byte[] sessionKey = body.getTicket().getAesKey();
        if (!body.getAuthenticator().encrypt(sessionKey)) {
            throw new RuntimeException("Unable to encrypt message");
        }
    }

    @Override
    public void decrypt(byte[] key) throws InvalidMessageException {
        if (this.body == null) {
            throw new RuntimeException("No body for the message, nothing to encrypt");
        }
        SubmitTicketRequestBody body = (SubmitTicketRequestBody) this.body;
        if (!body.getTicket().decrypt(key)) {
            throw new RuntimeException("Unable to encrypt message");
        }

        byte[] sessionKey = body.getTicket().getAesKey();
        if (!body.getAuthenticator().decrypt(sessionKey)) {
            throw new RuntimeException("Unable to encrypt message");
        }
    }
}
