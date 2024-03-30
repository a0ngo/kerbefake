package kerbefake.auth_server;

import kerbefake.common.ConnectionHandler;
import kerbefake.common.entities.MessageCode;
import kerbefake.common.entities.ServerMessage;
import kerbefake.common.entities.ServerRequest;

import java.net.Socket;

import static kerbefake.auth_server.AuthServer.authLogger;

/**
 * A connection handler that handles requests for the auth server.
 */
public class AuthServerConnectionHandler extends ConnectionHandler {

    public AuthServerConnectionHandler(Socket conn, Thread parentThread) {
        super(conn, parentThread, authLogger, new MessageCode[]{
                MessageCode.REGISTER_CLIENT,
                MessageCode.REQUEST_SYMMETRIC_KEY
        });
    }

    @Override
    public <T extends ServerMessage & ServerRequest> T processMessageBeforeExecution(T message) {
        return message;
    }
}
