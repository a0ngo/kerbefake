package kerbefake.auth_server.entities.responses.get_sym_key;

import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageHeader;

public class GetSymmetricKeyResponse extends ServerMessage {
    public GetSymmetricKeyResponse(ServerMessageHeader header, GetSymmetricKeyResponseBody body) {
        super(header, body);
    }
}
