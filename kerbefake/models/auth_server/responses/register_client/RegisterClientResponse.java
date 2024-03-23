package kerbefake.models.auth_server.responses.register_client;

import kerbefake.models.ServerMessage;
import kerbefake.models.ServerMessageHeader;

public class RegisterClientResponse extends ServerMessage {

    public RegisterClientResponse(ServerMessageHeader header, RegisterClientResponseBody body) {
        super(header, body);
    }


}
