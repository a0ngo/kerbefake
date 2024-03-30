package kerbefake.auth_server.entities.responses;

import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerMessageHeader;
import kerbefake.common.errors.InvalidMessageException;

import static kerbefake.auth_server.AuthServer.authLogger;
import static kerbefake.common.Constants.SERVER_VERSION;

public class FailureResponse extends ServerMessage {

    public FailureResponse(ServerMessageHeader header) {
        super(header, null);
    }

    public static FailureResponse createUnknownFailureResponse() {
        return new FailureResponse(new ServerMessageHeader(SERVER_VERSION, MessageCode.UNKNOWN_FAILURE, 0));
    }

    @Override
    public byte[] toLEByteArray() {
        try {
            return header.toLEByteArray();
        } catch (InvalidMessageException e) {
            // This will never happen since response does not have a hex string in its content.
            authLogger.error(e);
            return null;
        }
    }
}
