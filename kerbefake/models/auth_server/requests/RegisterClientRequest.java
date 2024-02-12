package kerbefake.models.auth_server.requests;

import kerbefake.models.auth_server.AuthServerRequestHeader;

public class RegisterClientRequest extends AuthServerRequest<RegisterClientRequestBody>{
    public RegisterClientRequest(AuthServerRequestHeader header, RegisterClientRequestBody body) {
        super(header, body);
    }

    @Override
    public void execute() {

    }
}
