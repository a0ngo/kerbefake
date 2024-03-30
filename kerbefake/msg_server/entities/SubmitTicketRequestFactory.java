package kerbefake.msg_server.entities;

import kerbefake.common.entities.*;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.common.Constants.SERVER_VERSION;

public final class SubmitTicketRequestFactory extends MessageFactory<SubmitTicketRequest> {

    private Authenticator authenticator;

    private Ticket ticket;

    private boolean encrypted;

    private static SubmitTicketRequestFactory instance;

    public static SubmitTicketRequestFactory getInstance() {
        return instance == null ? new SubmitTicketRequestFactory() : instance;
    }

    private SubmitTicketRequestFactory() {
        instance = this;
    }

    public SubmitTicketRequestFactory setAuthenticator(Authenticator authenticator) {
        if (this.authenticator != null)
            if (this.authenticator.isEncrypted())
                payloadSize -= this.authenticator.toLEByteArray().length;

        this.authenticator = authenticator;
        if (this.authenticator != null)
            if (this.authenticator.isEncrypted())
                payloadSize += this.authenticator.toLEByteArray().length;
        return this;
    }

    public SubmitTicketRequestFactory setTicket(Ticket ticket) throws InvalidMessageException {
        if (this.ticket != null)
            if (this.ticket.isEncrypted())
                payloadSize -= this.ticket.toLEByteArray().length;

        this.ticket = ticket;
        if (this.ticket != null)
            if (this.ticket.isEncrypted())
                payloadSize += this.ticket.toLEByteArray().length;
        return this;
    }

    public SubmitTicketRequestFactory encrypt(byte[] key) throws InvalidMessageException {
        if (this.authenticator == null)
            throw new InvalidMessageException("Missing authenticator for request.");
        if (this.ticket == null)
            throw new InvalidMessageException("Missing ticket for request.");

        if (!this.authenticator.isEncrypted())
            this.authenticator.encrypt(key);
        if (!this.ticket.isEncrypted())
            this.ticket.encrypt(key);

        encrypted = true;

        if (payloadSize == 0) {
            payloadSize += this.authenticator.toLEByteArray().length;
            payloadSize += this.ticket.toLEByteArray().length;
        }

        return this;
    }

    @Override
    protected SubmitTicketRequest internalBuild() throws InvalidMessageException {
        if (this.authenticator == null)
            throw new InvalidMessageException("Missing authenticator for request.");
        if (this.ticket == null)
            throw new InvalidMessageException("Missing ticket for request.");
        if (!encrypted)
            throw new InvalidMessageException("Must encrypt before building.");

        ServerMessageHeader header = new ServerMessageHeader(SERVER_VERSION, MessageCode.SUBMIT_TICKET, payloadSize);
        return new SubmitTicketRequest(header, new SubmitTicketRequestBody(authenticator, ticket));
    }
}
