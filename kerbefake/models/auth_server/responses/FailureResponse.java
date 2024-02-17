package kerbefake.models.auth_server.responses;

import kerbefake.models.auth_server.AuthServerMessage;
import kerbefake.models.auth_server.AuthServerMessageHeader;

public class FailureResponse extends AuthServerMessage {

    public FailureResponse(AuthServerMessageHeader header) {
        super(header, null);
    }

    @Override
    public byte[] toLEByteArray() {
        return new byte[0];
    }
}
