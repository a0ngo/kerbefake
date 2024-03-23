package kerbefake.models.msg_server.responses;

import kerbefake.models.ServerMessage;
import kerbefake.models.ServerMessageBody;
import kerbefake.models.ServerMessageHeader;

public class SubmitTicketResponse extends ServerMessage {
    public SubmitTicketResponse(ServerMessageHeader header, ServerMessageBody body) {
        super(header, body);
    }
}
