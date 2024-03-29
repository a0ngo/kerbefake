package kerbefake.common.entities;

public class EmptyResponse extends ServerMessage {
    public EmptyResponse(ServerMessageHeader header) {
        super(header, null);
    }
}
