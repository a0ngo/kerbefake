package kerbefake.client;

import kerbefake.errors.InvalidHexStringException;
import kerbefake.errors.InvalidMessageException;
import kerbefake.models.ServerMessage;

import java.io.*;
import java.net.Socket;

import static kerbefake.Logger.error;
import static kerbefake.Logger.info;

/**
 * A class that represents a single connection that the client has/
 * We assume that the connection is to either a kerberos server or to some message server.
 * This class is responsible for sending and receiving data to and from the connected server.
 */
public class ClientConnection {
    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public ClientConnection(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Connects to the server specified upon instantiation.
     *
     * @return true if connection was successful, false otherwise.
     */
    public boolean open() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            error("Failed to connect to server %s:%d due to: %s", serverAddress, serverPort, e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether this connection is open or not.
     *
     * @return - true if the underlying socket is still open, false otherwise.
     */
    public boolean isOpen() {
        return socket.isConnected();
    }

    /**
     * Gets the address of the server that we are connected to.
     *
     * @return the server address (ip:port).
     */
    public String getServerAddress() {
        return String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());
    }

    public ServerMessage send(ServerMessage message) throws InvalidMessageException, IOException, InvalidHexStringException {
        info("Sending message to server.");
        out.write(message.toLEByteArray());
        info("Waiting for server response.");
        return ServerMessage.parse(in);
    }

    public ServerMessage receiveMessageFromServer() throws IOException {
        //TODO: Fix
        return null;
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
            info("Connection to server %s:%d closed.", serverAddress, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            error("Failed closing connection to server %s:%d due to: %s", serverAddress, serverPort, e.getMessage());
        }
    }
}
