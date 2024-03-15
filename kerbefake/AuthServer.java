package kerbefake;

import kerbefake.models.auth_server.KnownPeers;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;

import static kerbefake.Constants.DEFAULT_PORT_AUTH_SERVER;
import static kerbefake.Logger.error;

/**
 * A class which handles all the functionality of the authentication server.
 */
public class AuthServer {

    /**
     * Starts the authentication server
     */
    public void start() {
        int port = loadPort();
        // Just to get it to load all the needed data before we start serving requests
        KnownPeers.getInstance();
        ServerSocket socket;
        try {
            socket = ServerSocketFactory.getDefault().createServerSocket();
            socket.bind(new InetSocketAddress("0.0.0.0", port));
        } catch (IOException e) {
            error("Failed to create or bind socket to default port (%d), due to: %s", port, e);
            return;
        }

        while (true) {
            Socket conn = null;
            try {
                conn = socket.accept();
            } catch (IOException e) {
                error("Failed to accept new connection due to: %s", e);
            }

            new Thread(new AuthServerConnectionHandler(conn)).start();
        }
    }


    /**
     * Loads the port from the file port.info
     *
     * @return the port stored in port.info, or 1256 in case no such file exists or it is empty
     */
    private int loadPort() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("./port.info")));
            String portStr = reader.readLine();

            return Integer.parseInt(portStr);
        } catch (FileNotFoundException e) {
            error("No file port.info, using default port (%d)", DEFAULT_PORT_AUTH_SERVER);
        } catch (IOException e) {
            error("No data in port.info, using default port (%d)", DEFAULT_PORT_AUTH_SERVER);
        } catch (NumberFormatException e) {
            error("Invalid port number in port.info, using default port (%d)", DEFAULT_PORT_AUTH_SERVER);
        }
        return DEFAULT_PORT_AUTH_SERVER;
    }

}
