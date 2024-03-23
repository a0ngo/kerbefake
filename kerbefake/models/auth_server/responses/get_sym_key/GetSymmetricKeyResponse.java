package kerbefake.models.auth_server.responses.get_sym_key;

import kerbefake.models.ServerMessage;
import kerbefake.models.ServerMessageHeader;

public class GetSymmetricKeyResponse extends ServerMessage {
    public GetSymmetricKeyResponse(ServerMessageHeader header, GetSymmetricKeyResponseBody body) {
        super(header, body);
    }
}
