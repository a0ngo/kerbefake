package kerbefake.msg_server;

import javax.net.ServerSocketFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.Base64;

import static kerbefake.Logger.error;
import static kerbefake.Logger.info;

/**
 * A class representing a print server.
 */
public class MessageServer {

    /**
     * This server's ID.
     */
    private String serverId;

    /**
     * The symmetric key used by the server to decrypt messages
     */
    private byte[] key;

    /**
     * The socket used to serve incoming connections.
     */
    private ServerSocket socket;

    public MessageServer() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("msg.info"));
        String addr = reader.readLine();
        String name = reader.readLine();
        String serverId = reader.readLine();
        String keyBase64 = reader.readLine();

        if (addr == null || addr.isEmpty()) {
            throw new RuntimeException("No address information in msg.info file.");
        }

        if (serverId == null || serverId.length() != 32) {
            throw new RuntimeException("No server ID in msg.info file.");
        }

        if (keyBase64 == null || keyBase64.isEmpty()) {
            throw new RuntimeException("No key in msg.info file.");
        }

        info("%s server starting", name);

        String[] ipComps = addr.split(":");
        int port;
        try {
            port = Integer.parseInt(ipComps[1], 10);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Port is not a decimal number.");
        }

        this.socket = ServerSocketFactory.getDefault().createServerSocket();
        this.socket.setSoTimeout(1000); // 1 second timeout
        this.socket.bind(new InetSocketAddress(ipComps[0], port));
        this.serverId = serverId;
        this.key = Base64.getDecoder().decode(keyBase64);
    }

    public void start() {
        while (!Thread.currentThread().isInterrupted()) {
            Socket incoming;
            try {
                incoming = this.socket.accept();
            } catch (SocketTimeoutException e) {
                // We simply continue since we set a 1 second timeout to make sure we stop the server.
                continue;
            } catch (IOException e) {
                error("Failed to accept connection due to: %s", e);
                continue; // We hope this is fixed on its own.
            }

            new Thread(new MessageServerConnectionHandler(incoming, Thread.currentThread(), this.key)).start();
        }
    }

}
