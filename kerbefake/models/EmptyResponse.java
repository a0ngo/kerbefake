package kerbefake.models;

public class EmptyResponse extends ServerMessage {
    public EmptyResponse(ServerMessageHeader header) {
        super(header, null);
    }
}
