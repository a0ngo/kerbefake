package kerbefake.models.auth_server.responses.register_client;

import kerbefake.Utils;
import kerbefake.errors.InvalidResponseDataException;
import kerbefake.models.auth_server.AuthServerMessageBody;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static kerbefake.Utils.strToLEByteArray;

public class RegisterClientResponseBody extends AuthServerMessageBody {

    private String id;

    public RegisterClientResponseBody() {
    }

    public RegisterClientResponseBody(String id) {
        this.id = id;
    }

    @Override
    public AuthServerMessageBody parse(byte[] bodyBytes) throws Exception {
        if (bodyBytes.length != 16) {
            throw new InvalidResponseDataException("Id");
        }

        this.id = ByteBuffer.wrap(bodyBytes).order(ByteOrder.LITTLE_ENDIAN).toString();
        return this;
    }

    @Override
    public byte[] toLEByteArray() {
        return strToLEByteArray(this.id);
    }

    @Override
    public String toString() {
        return "RegisterClientResponseBody{" +
                "id='" + id + '\'' +
                '}';
    }
}