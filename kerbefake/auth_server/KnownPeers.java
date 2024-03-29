package kerbefake.auth_server;

import kerbefake.Constants;
import kerbefake.errors.InvalidClientDataException;
import kerbefake.errors.InvalidMessageServerDataException;
import kerbefake.models.auth_server.ClientEntry;
import kerbefake.models.auth_server.MessageServerEntry;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static kerbefake.Constants.CLIENTS_FILE_NAME;
import static kerbefake.Constants.SERVER_CONFIG_FILE_NAME;
import static kerbefake.Logger.*;

public final class KnownPeers {

    private static KnownPeers instance;

    private final Map<String, ClientEntry> clients;

    private final Map<String, MessageServerEntry> servers;

    private KnownPeers() {
        clients = Collections.synchronizedMap(new HashMap<>());
        servers = Collections.synchronizedMap(new HashMap<>());
        readAllClients();
        if(clients.size() == 0){
            try {
                new BufferedWriter(new FileWriter(CLIENTS_FILE_NAME, false)).close();
            } catch (IOException e) {
                error("Couldn't reset client file.");
                throw new RuntimeException(e);
            }
        }
        readMsgServerData();
        instance = this;
    }

    /**
     * Reads and parses all the clients from the ./clients file.
     * In case of an error the {@link KnownPeers#clients} field will be cleared and then flushed (i.e. the clients will be reset)
     * The data is stored with the following structure:
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
    private void readAllClients() {
        BufferedReader clientReader;
        try {
            clientReader = new BufferedReader(new FileReader(CLIENTS_FILE_NAME));
        } catch (FileNotFoundException e) {
            info("No clients file found.");
            return;
        }
        String clientLine;
        while (true) {
            try {
                clientLine = clientReader.readLine();
            } catch (IOException e) {
                error("Failed to read line from file: %s", e);
                // We assume that if there was a read issue we can assume the file is corrupted therefore we won't be using the data in the file.
                clients.clear();
                return;
            }

            if (clientLine == null) {
                break;
            }

            try {
                ClientEntry client = ClientEntry.parseClient(clientLine);
                clients.put(client.getId(), client);
            } catch (InvalidClientDataException e) {
                error("Failed to parse client data, assuming corrupted file and returning no clients registered, due to: %s", e);
                clients.clear();
                return;
            }
        }
    }


    /**
     * Reads the msg.info file and returns an arraylist with all the registered message servers.
     *
     * @return an ArrayList of all the message server entries.
     */
    private void readMsgServerData() {
        BufferedReader serverReader;
        try {
            serverReader = new BufferedReader(new FileReader(SERVER_CONFIG_FILE_NAME));
        } catch (FileNotFoundException e) {
            error("Unable to find msg.info file, no messaging server provided.");
            throw new RuntimeException(e);
        }

        String ipAddr, name, id, b64SymKey;
        try {
            ipAddr = serverReader.readLine();
            name = serverReader.readLine();
            id = serverReader.readLine();
            b64SymKey = serverReader.readLine();
        } catch (IOException e) {
            error("Failed to read line from msg.info file, must have 4 lines in the following order:\nIP:port\nName\nId (hex)\nBase64 Symmetric key\nFailure happened due to: %s", e);
            throw new RuntimeException(e);
        }

        try {
            servers.put(id, MessageServerEntry.parseMessageEntryData(ipAddr, name, id, b64SymKey));
        } catch (InvalidMessageServerDataException e) {
            error("Failed to parse message server entry due to %e", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Tries to flush the entire known client list to a file (resets the file)
     */
    private void flushClientsFile() {
        debug("Flushing known clients to file");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(CLIENTS_FILE_NAME, false));
        } catch (IOException e) {
            error("Failed to open file to write clients due to: %s", e);
            return;
        }
        synchronized (clients) {
            clients.values().forEach(v -> {
                try {
                    writer.write(v.toString() + "\n");
                } catch (IOException e) {
                    error("Failed to write client to file due to: %s", e);
                }
            });
            try {
                writer.flush();
            } catch (IOException e) {
                error("Failed to flush client file");
            }
        }
    }

    /**
     * Tries to add a new client entry to the known clients map.
     * The method is synchronized and since the class is a singleton it will synchronize on the object for all callers / users of the class
     * The method will also persist the data
     *
     * @param entry - the entry to add
     * @return - false if failed, true if successful
     * @throws RuntimeException - in case of a UUID collision
     */
    public synchronized boolean tryAddClientEntry(ClientEntry entry) {
        debug("Trying to adding client entry: %s %s", entry.getId(), entry.getName());

        if (clients.values().stream().anyMatch(v -> v.getName().equals(entry.getName()))) {
            warn("Client with the same name already found.");
            return false;
        }

        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(CLIENTS_FILE_NAME, true));
        } catch (IOException e) {
            error("Failed to open file to write clients due to: %s", e);
            return false;
        }
        if (clients.containsKey(entry.getId())) {
            // This shouldn't happen as UUID is very unlikely to yield a collision
            throw new RuntimeException("Duplicate client ID detected!");
        }
        clients.put(entry.getId(), entry);

        try {
            writer.write(entry + "\n");
            writer.flush();
        } catch (IOException e) {
            error("Failed to write client to file due to: %s", e);
            return false;
        }


        return true;
    }

    // TODO: Code duplication cleanup
    public synchronized ClientEntry getClient(String clientID) {
        debug("Trying to fetch client: %s", clientID);
        List<ClientEntry> matchingClients = clients.values().stream().filter(v -> v.getId().equals(clientID)).collect(Collectors.toList());
        if (matchingClients.size() > 1) {
            throw new RuntimeException("More than a single client with the same ID found!");
        }
        return matchingClients.size() == 0 ? null : matchingClients.get(0);
    }

    public synchronized MessageServerEntry getSever(String serverId){
        debug("Trying to fetch server for ID: %s", serverId);
        List<MessageServerEntry> matchingServers = servers.values().stream().filter(v -> v.getId().equals(serverId)).collect(Collectors.toList());
        if(matchingServers.size() > 1){
            throw new RuntimeException("More than a single server with the same ID found!");
        }

        return matchingServers.size() == 0 ? null : matchingServers.get(0);
    }

    public static KnownPeers getInstance() {
        return instance == null ? new KnownPeers() : instance;
    }
}
