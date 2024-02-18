package kerbefake.models.auth_server.requests.get_sym_key;

import kerbefake.models.auth_server.AuthServerMessage;
import kerbefake.models.auth_server.AuthServerMessageHeader;

public class GetSymmetricKeyRequest extends AuthServerMessage {
    public GetSymmetricKeyRequest(AuthServerMessageHeader header, GetSymmetricKeyRequestBody body) {
        super(header, body);
    }
}
