package kerbefake.models.auth_server.responses.get_sym_key;

import kerbefake.models.auth_server.AuthServerMessage;
import kerbefake.models.auth_server.AuthServerMessageHeader;

public class GetSymmetricKeyResponse extends AuthServerMessage {
    public GetSymmetricKeyResponse(AuthServerMessageHeader header, GetSymmetricKeyResponseBody body) {
        super(header, body);
    }
}
