package kerbefake.models.auth_server.responses.register_client;

import kerbefake.models.auth_server.AuthServerMessage;
import kerbefake.models.auth_server.AuthServerMessageHeader;

public class RegisterClientResponse extends AuthServerMessage {

    public RegisterClientResponse(AuthServerMessageHeader header, RegisterClientResponseBody body) {
        super(header, body);
    }


}
