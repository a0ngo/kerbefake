package kerbefake.models.auth_server.requests;

import kerbefake.errors.InvalidClientDataException;
import kerbefake.errors.RequestExecutionException;
import kerbefake.models.auth_server.AuthServerRequestHeader;
import kerbefake.models.auth_server.ClientEntry;
import kerbefake.models.auth_server.KnownClients;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static kerbefake.Logger.error;
import static kerbefake.Utils.hexStrToStr;

public class RegisterClientRequest extends AuthServerRequest<RegisterClientRequestBody, String> {
    public RegisterClientRequest(AuthServerRequestHeader header, RegisterClientRequestBody body) {
        super(header, body);
    }

    @Override
    public String execute() throws RequestExecutionException {
        KnownClients clients = KnownClients.getInstance();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            error("Failed to create message digest for SHA-256 due to: %s", e);
            throw new RequestExecutionException(this.getClass().getName(), "Failed to generate message digest for sha-256");
        }
        byte[] passwordHash = digest.digest(this.body.getPassword().getBytes());
        String hexUUID = UUID.randomUUID().toString().replace("-","");
        String id = hexStrToStr(hexUUID);
        boolean addedClient = false;
        try {
            addedClient = clients.tryAddClientEntry(new ClientEntry(
                    id,
                    this.body.getName(),
                    new String(passwordHash),
                    Date.from(Instant.now())
            ));
        } catch (InvalidClientDataException e) {
            // Won't happen.
            error("Failed to create new client entry due to: %s", e);
            return null;
        }

        if (!addedClient) {
            throw new RequestExecutionException(this.getClass().getName(), "Client addition failed.");
        }

        return id;
    }


}
