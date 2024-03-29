package kerbefake.auth_server.entities.responses;

import kerbefake.common.errors.InvalidHexStringException;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageHeader;

public class FailureResponse extends ServerMessage {

    public FailureResponse(ServerMessageHeader header) {
        super(header, null);
    }

    @Override
    public byte[] toLEByteArray() throws InvalidHexStringException {
        return header.toLEByteArray();
    }
}
