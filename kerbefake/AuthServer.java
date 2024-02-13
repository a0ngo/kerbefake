package kerbefake;

import kerbefake.errors.InvalidClientDataException;
import kerbefake.errors.InvalidMessageServerDataException;
import kerbefake.models.auth_server.ClientEntry;
import kerbefake.models.auth_server.KnownClients;
import kerbefake.models.auth_server.MessageServerEntry;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

import static kerbefake.Constants.DEFAULT_PORT_AUTH_SERVER;
import static kerbefake.Logger.error;
import static kerbefake.Logger.info;

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
        KnownClients.getInstance();
        ArrayList<MessageServerEntry> knownMessageServers = readMsgServerData();
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

    /**
     * Reads the msg.info file and returns an arraylist with all the registered message servers.
     *
     * @return an ArrayList of all the message server entries.
     */
    private ArrayList<MessageServerEntry> readMsgServerData() {
        BufferedReader serverReader;
        ArrayList<MessageServerEntry> servers = new ArrayList<>();
        try {
            serverReader = new BufferedReader(new FileReader("msg.info"));
        } catch (FileNotFoundException e) {
            error("Unable to find msg.info file, no messaging server provided.");
            return servers;
        }

        String ipAddr, name, id, b64SymKey;
        try {
            ipAddr = serverReader.readLine();
            name = serverReader.readLine();
            id = serverReader.readLine();
            b64SymKey = serverReader.readLine();
        } catch (IOException e) {
            error("Failed to read line from msg.info file, must have 4 lines in the following order:\nIP:port\nName\nId (hex)\nBase64 Symmetric key\nFailure happened due to: %s", e);
            return servers;
        }

        try {
            servers.add(MessageServerEntry.parseMessageEntryData(ipAddr, name, id, b64SymKey));
        } catch (InvalidMessageServerDataException e) {
            error("Failed to parse message server entry due to %e", e);
            return servers;
        }

        return servers;
    }
}
