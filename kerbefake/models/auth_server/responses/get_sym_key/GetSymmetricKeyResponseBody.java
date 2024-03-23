package kerbefake.models.auth_server.responses.get_sym_key;

import kerbefake.errors.InvalidMessageException;
import kerbefake.models.EncryptedKey;
import kerbefake.models.EncryptedServerMessageBody;
import kerbefake.models.Ticket;
import kerbefake.models.ServerMessageBody;

import static kerbefake.Utils.*;

public class GetSymmetricKeyResponseBody extends ServerMessageBody {

    private String clientId;

    private EncryptedKey encKey;

    private Ticket ticket;

    public GetSymmetricKeyResponseBody() {
    }

    public GetSymmetricKeyResponseBody(String clientId, EncryptedKey encKey, Ticket ticket) {
        this.clientId = clientId;
        this.encKey = encKey;
        this.ticket = ticket;
    }

    @Override
    public ServerMessageBody parse(byte[] bodyBytes) throws Exception {
        this.clientId = bytesToHexString(byteArrayToLEByteBuffer(bodyBytes, 0, 16).array());

        // 16 + 48 = 16 byte IV + 48 byte encrypted data.
        this.encKey = new EncryptedKey().parse(byteArrayToLEByteBuffer(bodyBytes, 16, 16 + 48).array());

        // 41 byte (1 version, 16 client ID, 16 server ID, 8 creation time) metadata + 16 byte ticket Iv + 48 byte encrypted data
        this.ticket = new Ticket().parse(byteArrayToLEByteBuffer(bodyBytes, 16 + 16 + 48, 41 + 16 + 48).array());

        return this;
    }

    @Override
    public byte[] toLEByteArray() {
        byte[] encKeyBytes = encKey.toLEByteArray();
        byte[] ticketBytes = ticket.toLEByteArray();

        byte[] byteArr = new byte[clientId.length() + encKeyBytes.length + ticketBytes.length];
        int offset = 0;
        System.arraycopy(hexStringToByteArray(clientId), 0, byteArr, 0, 16);
        offset += 16;
        System.arraycopy(encKeyBytes, 0, byteArr, offset, encKeyBytes.length);
        offset += encKeyBytes.length;
        System.arraycopy(ticketBytes, 0, byteArr, offset, ticketBytes.length);

        return byteArrayToLEByteBuffer(byteArr).array();
    }

    @Override
    public String toString() {
        return "GetSymmetricKeyResponseBody{" +
                "clientId='" + clientId + '\'' +
                ", encKey=" + encKey +
                ", ticket=" + ticket +
                '}';
    }

    public String getClientId() {
        return clientId;
    }

    public EncryptedKey getEncKey() {
        return encKey;
    }

    public Ticket getTicket() {
        return ticket;
    }
}
