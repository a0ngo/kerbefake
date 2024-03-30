package kerbefake.auth_server.entities.requests.register_client;

import kerbefake.common.Utils;
import kerbefake.common.errors.InvalidMessageCodeException;
import kerbefake.common.entities.ServerMessageBody;

import static kerbefake.common.Utils.getNullTerminatedStringFromByteArray;

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
        String nameWithNullTermination = this.name+"\0";
        String passwordWithNullTermination = new String(this.password)+"\0";

        this.rawBody = new byte[nameWithNullTermination.length() + passwordWithNullTermination.length()];
        byte[] nameBytes = nameWithNullTermination.getBytes();
        byte[] passBytes = new byte[passwordWithNullTermination.length()];
        for (int i = 0; i < passwordWithNullTermination.length(); i++) {
            passBytes[i] = (byte) passwordWithNullTermination.charAt(i);
        }
        passBytes[passBytes.length-1] = (byte)'\0';
        System.arraycopy(nameBytes, 0, this.rawBody, 0, nameBytes.length);
        System.arraycopy(passBytes, 0, this.rawBody, nameBytes.length, passBytes.length);

        return this.rawBody;
    }
}
