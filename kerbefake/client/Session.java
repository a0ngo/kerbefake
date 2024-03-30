package kerbefake.client;

import kerbefake.common.entities.Authenticator;
import kerbefake.common.entities.EncryptedKey;
import kerbefake.common.entities.Ticket;
import kerbefake.common.errors.InvalidMessageException;

import java.security.SecureRandom;

import static kerbefake.common.Constants.IV_SIZE;

/**
 * A class that represents a single session with a given server.
 * This holds the encrypted key and the ticket to send to the server.
 * This also allows for the creation of any needed objects to communicate with the server.
 */
public final class Session {

    private EncryptedKey key;

    private Ticket ticket;

    private String serverId;

    public Session(EncryptedKey key, Ticket ticket, String serverId) {
        this.key = key;
        this.ticket = ticket;
        this.serverId = serverId;
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

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Authenticator createAuthenticator(String clientId) throws InvalidMessageException {
        SecureRandom srand = new SecureRandom();
        byte[] iv = new byte[IV_SIZE];
        srand.nextBytes(iv);
        long creationTime = System.currentTimeMillis();
        return new Authenticator(iv, clientId, serverId, creationTime);
    }

    public byte[] getSessionKey() {
        return key.getAesKey();
    }
}