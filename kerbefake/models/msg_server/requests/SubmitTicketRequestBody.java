package kerbefake.models.msg_server.requests;

import kerbefake.models.Authenticator;
import kerbefake.models.ServerMessageBody;
import kerbefake.models.Ticket;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

public class SubmitTicketRequestBody extends ServerMessageBody {

    private Authenticator authenticator;

    private Ticket ticket;


    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public Ticket getTicket() {
        return ticket;
    }

    @Override
    public ServerMessageBody parse(byte[] bodyBytes) throws Exception {

        // 16 + 64 which is the next multiple of the decrypted size after padding.
        this.authenticator = new Authenticator().parse(byteArrayToLEByteBuffer(bodyBytes, 0, 16 + 64).array());
        // 41 byte (1 version, 16 client ID, 16 server ID, 8 creation time) metadata + 16 byte ticket Iv + 48 byte encrypted data
        this.ticket = new Ticket().parse(byteArrayToLEByteBuffer(bodyBytes, 16 + 64, 41 + 16 + 48).array());

        return this;
    }

    @Override
    public byte[] toLEByteArray() {
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
