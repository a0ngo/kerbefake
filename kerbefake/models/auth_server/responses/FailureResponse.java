package kerbefake.models.auth_server.responses;

import kerbefake.models.ServerMessage;
import kerbefake.models.ServerMessageHeader;

public class FailureResponse extends ServerMessage {

    public FailureResponse(ServerMessageHeader header) {
        super(header, null);
    }

    @Override
    public byte[] toLEByteArray() {
        return header.toLEByteArray();
    }
}
