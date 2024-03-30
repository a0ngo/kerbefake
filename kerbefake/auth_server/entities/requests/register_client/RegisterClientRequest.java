package kerbefake.auth_server.entities.requests.register_client;

import kerbefake.auth_server.KnownPeers;
import kerbefake.auth_server.entities.ClientEntry;
import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponse;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponseBody;
import kerbefake.auth_server.errors.InvalidClientDataException;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.entities.ServerRequest;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static kerbefake.auth_server.AuthServer.authLogger;
import static kerbefake.common.CryptoUtils.performSha256;

public class RegisterClientRequest extends ServerMessage implements ServerRequest {


    public RegisterClientRequest(ServerMessageHeader header, RegisterClientRequestBody body) {
        super(header, body);
    }

    @Override
    public ServerMessage execute() {
        RegisterClientRequestBody body = (RegisterClientRequestBody) this.body;
        KnownPeers clients = KnownPeers.getInstance();

        FailureResponse failedResponse = new FailureResponse(this.header.toResponseHeader(MessageCode.REGISTER_CLIENT_FAILED, 0));


        authLogger.info("Trying to execute register client request.");
        byte[] passwordHash;
        try {
            passwordHash = performSha256(body.getPassword());
        } catch (NoSuchAlgorithmException e) {
            authLogger.error(e);
            authLogger.error("No SHA-256 digest on this machine, can't proceed.");
            return failedResponse;
        }
        String id = UUID.randomUUID().toString().replace("-", "");
        boolean addedClient;
        try {
            addedClient = clients.tryAddClientEntry(new ClientEntry(
                    id,
                    body.getName(),
                    passwordHash,
                    Date.from(Instant.now())
            ));
        } catch (InvalidClientDataException e) {
            authLogger.error(e);
            authLogger.error("Failed to create new client entry due to: %s", e);
            return failedResponse;
        }

        if (!addedClient) {
            authLogger.error("Client addition failed.");
            return failedResponse;
        }

        return new RegisterClientResponse(this.header.toResponseHeader(MessageCode.REGISTER_CLIENT_SUCCESS, 16), new RegisterClientResponseBody(id));
    }
}
