package kerbefake.auth_server.entities;

import kerbefake.auth_server.errors.InvalidClientDataException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import static kerbefake.common.Constants.ID_LENGTH;
import static kerbefake.common.Logger.error;

/**
 * Defines a
 */
public class ClientEntry {

    private String id;
    private String name;
    private byte[] passwordHash;
    private Date lastSeen;

    public ClientEntry(String id, String name, byte[] passwordHash, Date lastSeen) throws InvalidClientDataException {
        if (id.length() != ID_LENGTH || !id.matches("^[0-9a-f]{32}$")) {
            throw new InvalidClientDataException("Id");
        }
        if (passwordHash.length != 32) {
            throw new InvalidClientDataException("Password");
        }

        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        this.lastSeen = lastSeen;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public Date getLastSeen() {
        return lastSeen;
    }


    /**
     * Parses a line from the clients file and creates a client entry.
     *
     * @param clientData - The line from the client's file
     * @return - A client entry parsed and validated from the file.
     * @throws InvalidClientDataException - in case the entry string provided fails validation.
     */
    public static ClientEntry parseClient(String clientData) throws InvalidClientDataException {
        String[] clientLineParts = clientData.split(":");
        if (clientLineParts.length != 4) {
            error("Invalid data line, invalid number of colons, has %d expected 3, assuming corrupted file and returning empty values ", clientLineParts.length - 1);
            throw new InvalidClientDataException("Entry");
        }

        String id = clientLineParts[0];
        String name = clientLineParts[1];
        byte[] passHash = Base64.getDecoder().decode(clientLineParts[2]);
        String lastSeen = clientLineParts[3];

        if (id.length() != 32)
            throw new InvalidClientDataException("Id");
        if (name.length() > 255)
            throw new InvalidClientDataException("Name");
        if (passHash.length != 32)
            throw new InvalidClientDataException("Password Hash");

        if (lastSeen.length() != 19)
            throw new InvalidClientDataException("Last Seen");
        Date lastSeenDate;
        try {
            lastSeenDate = new SimpleDateFormat("hh.mm.ss dd/MM/yyyy").parse(lastSeen);
        } catch (ParseException e) {
            throw new InvalidClientDataException("Date");
        }


        return new ClientEntry(id, name, passHash, lastSeenDate);
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s:%s", id, name, new String(Base64.getEncoder().encode(passwordHash)), new SimpleDateFormat("hh.mm.ss dd/MM/yyyy").format(lastSeen));
    }
}
