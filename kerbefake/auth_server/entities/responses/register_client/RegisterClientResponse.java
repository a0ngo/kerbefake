package kerbefake.auth_server.entities.responses.register_client;

import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageHeader;

public class RegisterClientResponse extends ServerMessage {

    public RegisterClientResponse(ServerMessageHeader header, RegisterClientResponseBody body) {
        super(header, body);
    }


}
