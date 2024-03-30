package kerbefake.auth_server;

import kerbefake.common.ConnectionHandler;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerRequest;

import java.net.Socket;

/**
 * A connection handler that handles requests for the auth server.
 */
public class AuthServerConnectionHandler extends ConnectionHandler {

    public AuthServerConnectionHandler(Socket conn, Thread parentThread) {
        super(conn, parentThread);
    }

    @Override
    public <T extends ServerMessage & ServerRequest> T processMessageBeforeExecution(T message) {
        return message;
    }
}
