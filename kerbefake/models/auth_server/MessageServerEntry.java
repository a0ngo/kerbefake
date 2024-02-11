package kerbefake.models.auth_server;

import kerbefake.errors.InvalidMessageServerDataException;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Base64;
import java.util.Objects;

import static kerbefake.Logger.error;

public class MessageServerEntry {

    private Socket addr;
    private String name;
    private String id;
    private byte[] symmetricKey;

    public MessageServerEntry(Socket addr, String name, String id, byte[] symKey) {
        this.addr = addr;
        this.name = name;
        this.id = id;
        this.symmetricKey = symKey;
    }

    public Socket getAddr() {
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

        String ip = ipAddrComponents[0];
        int port;

        try {
            port = Integer.parseInt(ipAddrComponents[1]);
        } catch (NumberFormatException e) {
            throw new InvalidMessageServerDataException("Port in Ip");
        }

        Socket ipAddr;
        try {
            ipAddr = new Socket(ip, port);
        } catch (IOException e) {
            error("Failed to create a socket for the IP of the messsage server.");
            throw new InvalidMessageServerDataException("Ip");
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

        return new MessageServerEntry(ipAddr, name, id, symKey);
    }
}
