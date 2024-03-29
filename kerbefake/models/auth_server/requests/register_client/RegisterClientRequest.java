package kerbefake.models.auth_server.requests.register_client;

import kerbefake.auth_server.KnownPeers;
import kerbefake.errors.InvalidClientDataException;
import kerbefake.models.MessageCode;
import kerbefake.models.ServerMessage;
import kerbefake.models.ServerMessageHeader;
import kerbefake.models.auth_server.*;
import kerbefake.models.ServerRequest;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponseBody;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static kerbefake.Logger.error;
import static kerbefake.Logger.info;
import static kerbefake.Utils.performSha256OnValue;

public class RegisterClientRequest extends ServerMessage implements ServerRequest {


    public RegisterClientRequest(ServerMessageHeader header, RegisterClientRequestBody body) {
        super(header, body);
    }

    @Override
    public ServerMessage execute() {
        RegisterClientRequestBody body = (RegisterClientRequestBody) this.body;
        KnownPeers clients = KnownPeers.getInstance();
        MessageDigest digest;

        FailureResponse failedResponse = new FailureResponse(this.header.toResponseHeader(MessageCode.REGISTER_CLIENT_FAILED, 0));


        info("Trying to execute register client request.");
        byte[] passwordHash;
        try {
            passwordHash = performSha256OnValue(body.getPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            error("No SHA-256 digest on this machine, can't proceed.");
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
            e.printStackTrace();
            error("Failed to create new client entry due to: %s", e);
            return failedResponse;
        }

        if (!addedClient) {
            error("Client addition failed.");
            return failedResponse;
        }

        return new RegisterClientResponse(this.header.toResponseHeader(MessageCode.REGISTER_CLIENT_SUCCESS, 16), new RegisterClientResponseBody(id));
    }
}
