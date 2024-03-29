package kerbefake.msg_server;

import kerbefake.common.entities.Ticket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static kerbefake.common.Logger.error;

/**
 * A class representing all known sessions and their keys to the message server
 */
public final class KnownSessions {

    private final Map<String, Ticket> userIdToKeyMapping = Collections.synchronizedMap(new HashMap<>());

    private static KnownSessions instance;

    public static KnownSessions getInstance() {
        return instance == null ? new KnownSessions() : instance;
    }

    private KnownSessions() {
        instance = this;
    }

    public synchronized boolean addSession(String clientId, Ticket ticket) {
        if (this.userIdToKeyMapping.containsKey(clientId)) {
            error("Client ID already exists in session mapping, ignoring.");
            return false;
        }

        this.userIdToKeyMapping.put(clientId, ticket);
        return true;
    }

    /**
     * Gets the ticket for a session with a given client.
     *
     * @param clientId - the client Id to find in the mapping
     * @return the ticket for the session, null if there isn't one.
     */
    public synchronized Ticket getSession(String clientId) {
        return this.userIdToKeyMapping.getOrDefault(clientId, null);
    }

}
