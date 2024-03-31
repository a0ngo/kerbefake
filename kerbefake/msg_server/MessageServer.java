package kerbefake.msg_server;

import kerbefake.common.Logger;

import javax.net.ServerSocketFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Base64;

/**
 * A class representing a print server.
 */
public class MessageServer {

    /**
     * The symmetric key used by the server to decrypt messages
     */
    private final byte[] key;

    /**
     * The socket used to serve incoming connections.
     */
    private final ServerSocket socket;

    public static Logger msgLogger = Logger.getLogger(Logger.LoggerType.MESSAGE_SERVER_LOGGER);

    public MessageServer() throws IOException {
        this(false);
    }

    public MessageServer(boolean fullDebug) throws IOException {
        if (fullDebug)
            msgLogger.updateMinimalLogLevel(Logger.LogLevel.DEBUG, Logger.LogLevel.DEBUG);
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

        msgLogger.info("%s server starting", name);

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
                msgLogger.error("Failed to accept connection due to: %s", e);
                continue; // We hope this is fixed on its own.
            }

            new Thread(new MessageServerConnectionHandler(incoming, Thread.currentThread(), this.key)).start();
        }

        try {
            socket.close();
        } catch (IOException e) {
            msgLogger.error("Failed to close socket due to: %s", e);
            throw new RuntimeException(e);
        }

    }

}
