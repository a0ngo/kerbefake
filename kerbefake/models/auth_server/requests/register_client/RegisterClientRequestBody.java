package kerbefake.models.auth_server.requests.register_client;

import kerbefake.Utils;
import kerbefake.errors.InvalidMessageCodeException;
import kerbefake.models.ServerMessageBody;

import static kerbefake.Utils.getNullTerminatedCharArrayFromByteArray;
import static kerbefake.Utils.getNullTerminatedStringFromByteArray;

public class RegisterClientRequestBody extends ServerMessageBody {

    private byte[] rawBody;

    private String name;

    private char[] password;

    public RegisterClientRequestBody() {
    }

    public RegisterClientRequestBody(String name, char[] password) {
        this.name = name;
        this.password = password;
        this.toLEByteArray();
    }

    private RegisterClientRequestBody setName(String name) {
        this.name = name;
        return this;
    }

    private RegisterClientRequestBody setPassword(char[] password) {
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

    public char[] getPassword() {
        return password;
    }

    public byte[] getRawBody() {
        return this.rawBody;
    }

    @Override
    public ServerMessageBody parse(byte[] bytes) throws InvalidMessageCodeException {
        if (bytes == null || bytes.length > 255 * 2) {
            throw new InvalidMessageCodeException("Body Size");
        }

        String name = getNullTerminatedStringFromByteArray(bytes);
        if (name == null || name.isEmpty()) {
            throw new InvalidMessageCodeException("Register Client Name");
        }
        char[] password = Utils.getNullTerminatedCharArrayFromByteArray(bytes, name.length() + 1);

        if (password == null || password.length == 0) {
            throw new InvalidMessageCodeException("Register Client Password");
        }

        return new RegisterClientRequestBody().setRawBody(bytes).setName(name).setPassword(password);
    }

    @Override
    public byte[] toLEByteArray() {
        if (this.rawBody != null) {
            return this.rawBody;
        }

        this.rawBody = new byte[this.name.length() + this.password.length];
        byte[] name = this.name.getBytes();
        byte[] pass = new byte[password.length];
        for (int i = 0; i < pass.length; i++) {
            pass[i] = (byte) password[i];
        }

        System.arraycopy(name, 0, this.rawBody, 0, name.length);
        System.arraycopy(pass, 0, this.rawBody, name.length, pass.length);

        return this.rawBody;
    }
}
