package kerbefake.models.auth_server.requests;

import kerbefake.errors.InvalidRequestException;

import static kerbefake.Utils.getNullTerminatedStringFromByteArray;

public class RegisterClientRequestBody extends AuthServerRequestBody{

    private byte[] rawBody;

    private String name;

    private String password;

    private RegisterClientRequestBody setName(String name) {
        this.name = name;
        return this;
    }

    private RegisterClientRequestBody setPassword(String password) {
        this.password = password;
        return this;
    }

    public RegisterClientRequestBody setRawBody(byte[] rawBody) {
        this.rawBody = rawBody;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public byte[] getRawBody() {
        return this.rawBody;
    }

    @Override
    protected AuthServerRequestBody parse(byte[] bytes) throws InvalidRequestException {
        if(bytes == null || bytes.length > 255*2) {
            throw new InvalidRequestException("Body Size");
        }

        String name = getNullTerminatedStringFromByteArray(bytes);
        if(name == null || name.length() == 0){
            throw new InvalidRequestException("Register Client Name");
        }
        String password = getNullTerminatedStringFromByteArray(bytes, name.length() + 1);

        if(password == null || password.length() == 0){
            throw new InvalidRequestException("Register Client Password");
        }

         return new RegisterClientRequestBody().setRawBody(bytes).setName(name).setPassword(password);



    }
}
