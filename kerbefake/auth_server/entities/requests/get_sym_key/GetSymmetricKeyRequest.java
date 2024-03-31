package kerbefake.auth_server.entities.requests.get_sym_key;

import kerbefake.auth_server.KnownPeers;
import kerbefake.auth_server.entities.ClientEntry;
import kerbefake.auth_server.entities.MessageServerEntry;
import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.auth_server.entities.responses.get_sym_key.GetSymmetricKeyResponse;
import kerbefake.auth_server.entities.responses.get_sym_key.GetSymmetricKeyResponseBody;
import kerbefake.common.entities.*;
import kerbefake.common.errors.InvalidMessageException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static kerbefake.auth_server.AuthServer.authLogger;
import static kerbefake.common.CryptoUtils.getIv;
import static kerbefake.common.CryptoUtils.getSecureRandomBytes;

public class GetSymmetricKeyRequest extends ServerMessage implements ServerRequest {
    public GetSymmetricKeyRequest(ServerMessageHeader header, GetSymmetricKeyRequestBody body) {
        super(header, body);
    }

    @Override
    public ServerMessage execute() throws InvalidMessageException {
        FailureResponse failure = new FailureResponse(this.header.toResponseHeader(MessageCode.UNKNOWN_FAILURE, 0));
        KnownPeers peers = KnownPeers.getInstance();
        ClientEntry client;
        MessageServerEntry server;
        if ((client = peers.getClient(header.getClientID())) == null) {
            authLogger.error("Client ID is not known!");
            return failure;
        }

        if (this.header == null || this.body == null) {
            authLogger.error("No header or body detected for get symmetric key request.");
            return failure;
        }

        GetSymmetricKeyRequestBody body = (GetSymmetricKeyRequestBody) this.body;

        String serverId = body.getServerId();
        if ((server = peers.getSever(serverId)) == null) {
            authLogger.error("Server ID is not known!");
            return failure;
        }


        byte[] aesKey = getSecureRandomBytes(32);
        byte[] ticketIv = getIv();
        byte[] clientIv = getIv();
        ;

        long time = System.currentTimeMillis();
        long expTime = time + 10 * 60 * 1000; // 10 minutes
         authLogger.debug("Exp time: %d", expTime);

        byte[] creationTimeArr = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(time).array();
        byte[] expTimeArr = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(expTime).array();

        EncryptedKey key = new EncryptedKey().setAesKey(aesKey).setNonce(body.getNonce()).setIv(clientIv);
        Ticket ticket = new Ticket().setTicketIv(ticketIv).setClientId(header.getClientID()).setServerId(serverId).setCreationTime(creationTimeArr).setAesKey(aesKey).setExpTime(expTimeArr);

        if (!key.encrypt(client.getPasswordHash())) {
            authLogger.error("Failed to encrypt EncryptedKey field for response");
            return failure;
        }

        if (!ticket.encrypt(server.getSymmetricKey())) {
            authLogger.error("Failed to encrypt Ticket field for response");
            return failure;
        }


        // First is client id (16 bytes) then enc key, and ticket with their respective sizes
        return new GetSymmetricKeyResponse(header.toResponseHeader(MessageCode.REQUEST_SYMMETRIC_KEY_SUCCESS, key.toLEByteArray().length + ticket.toLEByteArray().length + 16), new GetSymmetricKeyResponseBody(header.getClientID(), key, ticket));

    }
}
