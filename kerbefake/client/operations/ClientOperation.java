package kerbefake.client.operations;

import kerbefake.client.ClientConnection;
import kerbefake.common.errors.InvalidHexStringException;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.common.entities.ServerMessage;

import java.io.IOException;

/**
 * An abstract class that indicates something is an operation performed by the client.
 * This allows us to define that something is done by the client.
 *
 * @param <REQ> - the request type we send to the server
 * @param <RET> - the return type of the {@code perform}, for example the register will return a string to indicate the client code.
 */
public abstract class ClientOperation<REQ extends ServerMessage, RET> {


    private ClientConnection conn;

    protected ClientOperation(ClientConnection connection) {
        this.conn = connection;
    }

    /**
     * A method that simply takes a request that was prepared by some child class that implements {@link #perform()} and sends it, returning the response.
     *
     * @return the response from the server
     */
    protected ServerMessage internalPerform(REQ request) throws InvalidMessageException, InvalidHexStringException, IOException {
        return conn.send(request);
    }

    public abstract RET perform();

}
