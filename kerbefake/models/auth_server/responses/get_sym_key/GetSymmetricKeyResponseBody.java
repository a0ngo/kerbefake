package kerbefake.models.auth_server.responses.get_sym_key;

import kerbefake.errors.InvalidMessageException;
import kerbefake.models.EncryptedKey;
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
        if (bodyBytes.length != 185) {
            throw new InvalidMessageException(String.format("Invalid payload size, expected 169, got %d", bodyBytes.length));
        }

        this.clientId = bytesToHexString(byteArrayToLEByteBuffer(bodyBytes, 0, 16).array());
        this.encKey = EncryptedKey.parse(byteArrayToLEByteBuffer(bodyBytes, 16, EncryptedKey.SIZE).array());
        this.ticket = Ticket.parse(byteArrayToLEByteBuffer(bodyBytes, 16 + EncryptedKey.SIZE, Ticket.SIZE).array());

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
}
