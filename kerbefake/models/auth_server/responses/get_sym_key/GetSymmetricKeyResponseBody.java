package kerbefake.models.auth_server.responses.get_sym_key;

import kerbefake.Utils;
import kerbefake.errors.InvalidMessageException;
import kerbefake.models.EncryptedKey;
import kerbefake.models.Ticket;
import kerbefake.models.auth_server.AuthServerMessageBody;

import java.nio.charset.StandardCharsets;

import static kerbefake.Utils.byteArrayToLEByteBuffer;

public class GetSymmetricKeyResponseBody extends AuthServerMessageBody {

    private String clientId;

    private EncryptedKey encKey;

    private Ticket ticket;


    @Override
    public AuthServerMessageBody parse(byte[] bodyBytes) throws Exception {
        if (bodyBytes.length != 169) {
            throw new InvalidMessageException(String.format("Invalid payload size, expected 169, got %d", bodyBytes.length));
        }

        this.clientId = new String(byteArrayToLEByteBuffer(bodyBytes, 0, 16).array(), StandardCharsets.UTF_8);
        this.encKey = EncryptedKey.parse(byteArrayToLEByteBuffer(bodyBytes, 16, 56).array());
        this.ticket = Ticket.parse(byteArrayToLEByteBuffer(bodyBytes, 72, 97).array());

        return this;
    }

    @Override
    public byte[] toLEByteArray() {
        byte[] encKeyBytes = encKey.toLEByteArray();
        byte[] ticketBytes = ticket.toLEByteArray();

        byte[] byteArr = new byte[clientId.length() + encKeyBytes.length + ticketBytes.length];
        int offset = 0;
        System.arraycopy(clientId.getBytes(), 0, byteArr, 0, clientId.length());
        offset += clientId.length();
        System.arraycopy(encKeyBytes, 0, byteArr, offset, encKeyBytes.length);
        offset += encKeyBytes.length;
        System.arraycopy(ticketBytes, 0, byteArr, offset, ticketBytes.length);

        return byteArrayToLEByteBuffer(byteArr).array();
    }
}
