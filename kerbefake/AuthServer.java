package kerbefake;

import kerbefake.errors.InvalidClientDataException;
import kerbefake.models.auth_server.ClientEntry;
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
        HashMap<String, ClientEntry> knownClients = readClientData();
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
     * Obtains all client data from persistent storage.
     * The data is stored in the local file clients with the following structure:
     * ID:Name:PasswordHash:LastSeen
     * <p>
     * In order the size of the field is (bytes): 16,<=255,32,19
     * <p>
     * ID is 16 bytes.
     * Name is less than equals to 255 bytes (255 characters)
     * PasswordHash is 32 bytes
     * LastSeen is 19 bytes, format is (hh.mm.ss DD/MM/YYYY).
     *
     * @return An HashMap of clients if such data exists in persistent storage, if not an empty HashMap.
     */
    private HashMap<String, ClientEntry> readClientData() {
        BufferedReader clientReader;
        HashMap<String, ClientEntry> clients = new HashMap<>();
        try {
            clientReader = new BufferedReader(new FileReader(new File("./clients")));
        } catch (FileNotFoundException e) {
            info("No clients file found.");
            return clients;
        }
        String clientLine = null;
        while (true) {
            try {
                clientLine = clientReader.readLine();
            } catch (IOException e) {
                error("Failed to read line from file: %s", e);
                // We assume that if there was a read issue we can assume the file is corrupted therefore we won't be using the data in the file.
                clients.clear();
                return clients;
            }

            try {
                ClientEntry client = parseClient(clientLine);
                clients.put(client.getId(), client);
            } catch (InvalidClientDataException e) {
                error("Failed to parse client data, assuming corrupted file and returning no clients registered, due to: %s", e);
                clients.clear();
                return clients;
            }
        }
    }

    /**
     * Parses a line from the clients file.
     *
     * @param clientData - The line from the client's file
     * @return - A client entry parsed and validated from the file.
     * @throws InvalidClientDataException - in case the entry string provided fails validation.
     */
    private ClientEntry parseClient(String clientData) throws InvalidClientDataException {
        String[] clientLineParts = clientData.split(":");
        if (clientLineParts.length != 4) {
            error("Invalid data line, invalid number of colons, has %d expected 3, assuming corrupted file and returning empty values ", clientLineParts.length - 1);
            throw new InvalidClientDataException("Entry");
        }

        String id = clientLineParts[0];
        String name = clientLineParts[1];
        String passHash = clientLineParts[2];
        String lastSeen = clientLineParts[3];

        if (id.length() != 16)
            throw new InvalidClientDataException("Id");
        if (name.length() > 255)
            throw new InvalidClientDataException("Name");
        if (passHash.length() != 32)
            throw new InvalidClientDataException("Password Hash");
        if (lastSeen.length() != 19)
            throw new InvalidClientDataException("Last Seen");

        return new ClientEntry(id, name, passHash, lastSeen);
    }

    private ArrayList<MessageServerEntry> readMsgServerData() {
        return null;
    }
}
