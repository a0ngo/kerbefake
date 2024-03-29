package kerbefake.client;

import kerbefake.models.Authenticator;
import kerbefake.models.EncryptedKey;
import kerbefake.models.Ticket;

import java.security.SecureRandom;

import static kerbefake.Constants.IV_SIZE;

/**
 * A class that represents a single session with a given server.
 * This holds the encrypted key and the ticket to send to the server.
 * This also allows for the creation of any needed objects to communicate with the server.
 */
public final class Session {

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

    public Authenticator creatAuthenticator(String clientIdHex, String serverIdHex) {
        //TODO: implement
        return null;
    }
}