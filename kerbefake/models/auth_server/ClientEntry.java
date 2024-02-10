package kerbefake.models.auth_server;

import kerbefake.errors.InvalidClientDataException;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Defines a
 */
public class ClientEntry implements Serializable {

    private String id;
    private String name;
    private String passwordHash;
    private Date lastSeen;

    public ClientEntry(String id, String name, String passwordHash, String lastSeen) throws InvalidClientDataException {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        try {
            this.lastSeen = new SimpleDateFormat("hh.mm.ss DD/MM/YYYY").parse(lastSeen);
        } catch (ParseException e) {
            throw new InvalidClientDataException("Date");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Date getLastSeen() {
        return lastSeen;
    }
}
