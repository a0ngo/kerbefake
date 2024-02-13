package kerbefake.models.auth_server;

import kerbefake.Constants;
import kerbefake.errors.InvalidClientDataException;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static kerbefake.Logger.*;

public final class KnownClients {

    private static KnownClients instance;

    private final Map<String, ClientEntry> clients;

    private KnownClients(){
        clients = Collections.synchronizedMap(new HashMap<>());
        readAllClients();
        flushEntireFile();
        instance = this;
    }

    /**
     * Reads and parses all the clients from the ./clients file.
     * In case of an error the {@link KnownClients#clients} field will be cleared and then flushed (i.e. the clients will be reset)
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
    private void readAllClients(){
        BufferedReader clientReader;
        try {
            clientReader = new BufferedReader(new FileReader(Constants.CLIENTS_FILE_NAME));
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

            if(clientLine == null){
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
     * Tries to flush the entire known client list to a file (resets the file)
     */
    private void flushEntireFile(){
        debug("Flushing known clients to file");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(Constants.CLIENTS_FILE_NAME, false));
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
        }
    }

    /**
     * Tries to add a new client entry to the known clients map.
     * The method is synchronized and since the class is a singleton it will synchronize on the object for all callers / users of the class
     * The method will also persist the data
     * @param entry - the entry to add
     * @return - false if failed, true if successful
     * @throws RuntimeException - in case of a UUID collision
     */
    public synchronized boolean tryAddClientEntry(ClientEntry entry) {
        debug("Trying to adding client entry: %s %s", entry.getId(), entry.getName());

        if(clients.values().stream().anyMatch(v -> v.getName().equals(entry.getName()))){
            warn("Client with the same name already found.");
            return false;
        }

        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(Constants.CLIENTS_FILE_NAME, true));
        } catch (IOException e) {
            error("Failed to open file to write clients due to: %s", e);
            return false;
        }
        if(clients.containsKey(entry.getId())){
             // This shouldn't happen as UUID is very unlikely to yield a collision
            throw new RuntimeException("Duplicate client ID detected!");
        }
        clients.put(entry.getId(), entry);

        try {
            writer.write(entry + "\n");
        } catch (IOException e) {
            error("Failed to write client to file due to: %s", e);
            return false;
        }

        return true;
    }

    public static KnownClients getInstance() {
        return instance == null ? new KnownClients() : instance;
    }
}
