package kerbefake.models.auth_server;

import kerbefake.errors.InvalidMessageServerDataException;

import java.util.Base64;

public class MessageServerEntry {

    private String addr;
    private String name;
    private String id;
    private byte[] symmetricKey;

    public MessageServerEntry(String ipAddr, String name, String id, byte[] symKey) {
        this.addr = ipAddr;
        this.name = name;
        this.id = id;
        this.symmetricKey = symKey;
    }

    public String getAddr() {
        return addr;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public byte[] getSymmetricKey() {
        return symmetricKey;
    }

    /**
     * Parses 4 values corresponding to the entries in the msg.info file.
     *
     * @param addr      - The IP:port address of the message server
     * @param name      - The name of the messaging server
     * @param id        - The ID of the messaging server
     * @param b64SymKey - The base64 encoded symmetric key
     * @return A new {@link MessageServerEntry}
     * @throws InvalidMessageServerDataException - In case any provided data fails validation.
     */
    public static MessageServerEntry parseMessageEntryData(String addr, String name, String id, String b64SymKey) throws InvalidMessageServerDataException {
        if (addr == null)
            throw new InvalidMessageServerDataException("Ip");

        String[] ipAddrComponents = addr.split(":");
        if (ipAddrComponents.length != 2)
            throw new InvalidMessageServerDataException("Ip");

        try {
            Integer.parseInt(ipAddrComponents[1]); // Check port is a number
        } catch (NumberFormatException e) {
            throw new InvalidMessageServerDataException("Port in Ip");
        }


        if (name == null)
            throw new InvalidMessageServerDataException("Name");

        if (id == null)
            throw new InvalidMessageServerDataException("Id");
        if (!id.matches("^[a-fA-F0-9]+$"))
            throw new InvalidMessageServerDataException("Id");

        if (b64SymKey == null)
            throw new InvalidMessageServerDataException("Symmetric key");

        byte[] symKey;
        try {
            symKey = Base64.getDecoder().decode(b64SymKey);
        } catch (IllegalArgumentException e) {
            throw new InvalidMessageServerDataException("Symmetric Key");
        }

        return new MessageServerEntry(addr, name, id, symKey);
    }
}
