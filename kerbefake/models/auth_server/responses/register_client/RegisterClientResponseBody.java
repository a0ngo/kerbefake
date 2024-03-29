package kerbefake.models.auth_server.responses.register_client;

import kerbefake.errors.InvalidHexStringException;
import kerbefake.errors.InvalidResponseDataException;
import kerbefake.models.ServerMessageBody;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static kerbefake.Utils.*;

public class RegisterClientResponseBody extends ServerMessageBody {

    private String id;

    public RegisterClientResponseBody() {
    }

    public RegisterClientResponseBody(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public ServerMessageBody parse(byte[] bodyBytes) throws Exception {
        if (bodyBytes.length != 16) {
            throw new InvalidResponseDataException("Id");
        }

        this.id = bytesToHexString(ByteBuffer.wrap(bodyBytes).order(ByteOrder.LITTLE_ENDIAN).array());
        return this;
    }

    @Override
    public byte[] toLEByteArray() throws InvalidHexStringException  {
        return byteArrayToLEByteBuffer(hexStringToByteArray(this.id)).array();
    }

    @Override
    public String toString() {
        return "RegisterClientResponseBody{" +
                "id='" + id + '\'' +
                '}';
    }
}
