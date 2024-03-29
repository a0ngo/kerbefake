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
        serverIdToSessionMapping = new HashMap<>();

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

        Session newSession = new Session(encKey, ticket, serverId);
        serverIdToSessionMapping.put(serverId, newSession);
        return true;
    }

    /**
     * Gets the session used for this server Id if one exists.
     *
     * @param serverId - the server ID to look for
     * @return - the {@link Session} object if such exists, null otherwise
     */
    public Session getSession(String serverId) {
        return serverIdToSessionMapping.getOrDefault(serverId, null);
    }
}
