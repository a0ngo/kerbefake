package kerbefake.auth_server.entities.responses;

import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.common.Logger.error;

public class FailureResponse extends ServerMessage {

    public FailureResponse(ServerMessageHeader header) {
        super(header, null);
    }

    @Override
    public byte[] toLEByteArray() {
        try {
            return header.toLEByteArray();
        } catch (InvalidMessageException e) {
            // This will never happen since response does not have a hex string in its content.
            error(e);
            return null;
        }
    }
}
