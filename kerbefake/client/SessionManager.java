package kerbefake.client;

import kerbefake.models.EncryptedKey;
import kerbefake.models.Ticket;

import java.util.HashMap;
import java.util.Map;

import static kerbefake.Logger.error;

/**
 * A class that manages sessions that the client creates.
 * Since we didn't add the bonus, there is only a single session that needs to be managed thus a single session key.
 */
public final class SessionManager {

    private final Map<String, Session> serverIdToSessionMapping;
    private static SessionManager instance;

    public static SessionManager getInstance() {
        return instance == null ? new SessionManager() : instance;
    }

    private SessionManager() {
        instance = this;
        serverIdToSessionMapping = new HashMap<String, Session>();

    }

    public boolean createNewSession(String serverId, EncryptedKey encKey, Ticket ticket) {
        Session existingSession = serverIdToSessionMapping.get(serverId);
        if (existingSession != null) {
            error("Session already exists with this server, please restart the client to clear it.");
            return false;
        }

        if (encKey.isEncrypted()) {
            error("Encrypted key was not decrypted before creating the session.");
            return false;
        }

        Session newSession = new Session(encKey, ticket);
        serverIdToSessionMapping.put(serverId, newSession);
        return true;
    }

    /**
     * Fetches the session key for a given server id.
     *
     * @param serverId - the server ID to look for
     * @return - a byte array representing the key or null if no such session exists.
     */
    public byte[] getKeyForSession(String serverId) {
        Session existingSession = serverIdToSessionMapping.get(serverId);
        if (existingSession == null) {
            error("No session for: %s", serverId);
            return null;
        }

        return existingSession.key.getAesKey();
    }


    private static class Session {

        private EncryptedKey key;

        private Ticket ticket;

        public Session(EncryptedKey key, Ticket ticket) {
            this.key = key;
            this.ticket = ticket;
        }

        public EncryptedKey getKey() {
            return key;
        }

        public void setKey(EncryptedKey key) {
            this.key = key;
        }

        public Ticket getTicket() {
            return ticket;
        }

        public void setTicket(Ticket ticket) {
            this.ticket = ticket;
        }
    }
}
