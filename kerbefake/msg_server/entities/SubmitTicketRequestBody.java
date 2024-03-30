package kerbefake.msg_server.entities;

import kerbefake.common.entities.Authenticator;
import kerbefake.common.entities.ServerMessageBody;
import kerbefake.common.entities.Ticket;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.common.Utils.byteArrayToLEByteBuffer;

public class SubmitTicketRequestBody extends ServerMessageBody {

    private Authenticator authenticator;

    private Ticket ticket;

    public SubmitTicketRequestBody() {
    }

    public SubmitTicketRequestBody(Authenticator authenticator, Ticket ticket) {
        this.authenticator = authenticator;
        this.ticket = ticket;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public Ticket getTicket() {
        return ticket;
    }

    @Override
    public ServerMessageBody parse(byte[] bodyBytes) throws Exception {

        // 16 + 48 which is the next multiple of the decrypted size after padding.
        this.authenticator = new Authenticator().parse(byteArrayToLEByteBuffer(bodyBytes, 0, 16 + Authenticator.DATA_ENCRYPTED_SIZE).array());
        // 41 byte (1 version, 16 client ID, 16 server ID, 8 creation time) metadata + 16 byte ticket Iv + 48 byte encrypted data
        this.ticket = new Ticket().parse(byteArrayToLEByteBuffer(bodyBytes, 16 + 48, 41 + 16 + Ticket.DATA_ENCRYPTED_SIZE).array());

        return this;
    }

    @Override
    public byte[] toLEByteArray() throws InvalidMessageException {
        if (authenticator == null || ticket == null) {
            throw new RuntimeException("Missing authenticator or ticket data.");
        }
        byte[] authenticatorBytes = this.authenticator.toLEByteArray();
        byte[] ticketBytes = this.ticket.toLEByteArray();

        byte[] bytes = new byte[authenticatorBytes.length + ticketBytes.length];

        System.arraycopy(authenticatorBytes, 0, bytes, 0, authenticatorBytes.length);
        System.arraycopy(ticketBytes, 0, bytes, authenticatorBytes.length, ticketBytes.length);

        return byteArrayToLEByteBuffer(bytes).array();
    }
}
