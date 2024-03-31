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
        if (authenticator == null) {
            this.authenticator = null;
            return this;
        }
        if (this.authenticator != null && payloadSize != 0)
            if (this.authenticator.isEncrypted())
                payloadSize -= this.authenticator.toLEByteArray().length;

        this.authenticator = authenticator;
        if (this.authenticator.isEncrypted())
            payloadSize += this.authenticator.toLEByteArray().length;
        return this;
    }

    public SubmitTicketRequestFactory setTicket(Ticket ticket) throws InvalidMessageException {
        if (ticket == null) {
            this.ticket = null;
            return this;
        }
        if (this.ticket != null && payloadSize != 0)
            if (this.ticket.isEncrypted())
                payloadSize -= this.ticket.toLEByteArray().length;

        this.ticket = ticket;
        if (this.ticket.isEncrypted())
            payloadSize += this.ticket.toLEByteArray().length;
        return this;
    }

    public SubmitTicketRequestFactory encrypt(byte[] sessionKey) throws InvalidMessageException {
        if (this.authenticator == null)
            throw new InvalidMessageException("Missing authenticator for request.");
        if (this.ticket == null)
            throw new InvalidMessageException("Missing ticket for request.");

        if (!this.authenticator.isEncrypted())
            this.authenticator.encrypt(sessionKey);
        if (!this.ticket.isEncrypted())
            throw new InvalidMessageException("Ticket is not encrypted but should have been already (since we just pass the encrypted ticket to the server.");

        encrypted = true;

        payloadSize = this.authenticator.toLEByteArray().length;
        payloadSize += this.ticket.toLEByteArray().length;


        return this;
    }

    @Override
    protected SubmitTicketRequest internalBuild() throws InvalidMessageException {
        try {
            if (this.authenticator == null)
                throw new InvalidMessageException("Missing authenticator for request.");
            if (this.ticket == null)
                throw new InvalidMessageException("Missing ticket for request.");
            if (!encrypted)
                throw new InvalidMessageException("Must encrypt before building.");

            ServerMessageHeader header = new ServerMessageHeader(clientId, SERVER_VERSION, MessageCode.SUBMIT_TICKET, payloadSize);
            return new SubmitTicketRequest(header, new SubmitTicketRequestBody(authenticator, ticket));
        } finally {
            setAuthenticator(null).setTicket(null).setClientId(null);
        }
    }
}
