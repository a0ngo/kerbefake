package kerbefake.models.auth_server.requests.get_sym_key;

import kerbefake.models.*;
import kerbefake.models.auth_server.*;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.auth_server.responses.get_sym_key.GetSymmetricKeyResponse;
import kerbefake.models.auth_server.responses.get_sym_key.GetSymmetricKeyResponseBody;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;

import static kerbefake.Logger.error;

public class GetSymmetricKeyRequest extends ServerMessage implements ServerRequest {
    public GetSymmetricKeyRequest(ServerMessageHeader header, GetSymmetricKeyRequestBody body) {
        super(header, body);
    }

    @Override
    public ServerMessage execute() {
        FailureResponse failure = new FailureResponse(this.header.toResponseHeader(MessageCode.UNKNOWN_FAILURE, 0));
        KnownPeers peers = KnownPeers.getInstance();
        ClientEntry client;
        MessageServerEntry server;
        if ((client = peers.getClient(header.getClientID())) == null) {
            error("Client ID is not known!");
            return failure;
        }

        if (this.header == null || this.body == null) {
            error("No header or body detected for get symmetric key request.");
            return failure;
        }

        GetSymmetricKeyRequestBody body = (GetSymmetricKeyRequestBody) this.body;

        String serverId = body.getServerId();
        if ((server = peers.getSever(serverId)) == null) {
            error("Server ID is not known!");
            return failure;
        }


        byte[] aesKey = new byte[32];
        byte[] ticketIv = new byte[16];
        byte[] clientIv = new byte[16];
        SecureRandom sRand = new SecureRandom();
        sRand.nextBytes(aesKey);
        sRand.nextBytes(ticketIv);
        sRand.nextBytes(clientIv);

        long time = System.currentTimeMillis();
        long expTime = time + 10 * 60 * 1000; // 10 minutes

        byte[] creationTimeArr = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(time).array();
        byte[] expTimeArr = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(expTime).array();

        EncryptedKey key = new EncryptedKey().setAesKey(aesKey).setNonce(body.getNonce()).setIv(clientIv);
        Ticket ticket = new Ticket().setTicketIv(ticketIv).setClientId(header.getClientID()).setServerId(serverId).setCreationTime(creationTimeArr).setAesKey(aesKey).setExpTime(expTimeArr);

        if (!key.encrypt(client.getPasswordHash())) {
            error("Failed to encrypt EncryptedKey field for response");
            return failure;
        }

        if (!ticket.encrypt(server.getSymmetricKey())) {
            error("Failed to encrypt Ticket field for response");
            return failure;
        }


        // First is client id (16 bytes) then enc key, and ticket with their respective sizes
        return new GetSymmetricKeyResponse(header.toResponseHeader(MessageCode.REQUEST_SYMMETRIC_KEY_SUCCESS, key.toLEByteArray().length + ticket.toLEByteArray().length + 16), new GetSymmetricKeyResponseBody(header.getClientID(), key, ticket));

    }
}
