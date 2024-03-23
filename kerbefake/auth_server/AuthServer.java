package kerbefake.auth_server;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static kerbefake.Constants.DEFAULT_PORT_AUTH_SERVER;
import static kerbefake.Logger.error;

/**
 * A class which handles all the functionality of the authentication server.
 */
public class AuthServer {

    private final ArrayList<Thread> connectionHandles = new ArrayList<>();

    /**
     * Starts the authentication server
     */
    public void start() {
        int port = loadPort();
        // Just to get it to load all the needed data before we start serving requests
        KnownPeers.getInstance();
        ServerSocket socket;
        try {
            socket = ServerSocketFactory.getDefault().createServerSocket();
            socket.bind(new InetSocketAddress("127.0.0.1", port));
            socket.setSoTimeout(1000); // 1 second timeout
        } catch (IOException e) {
            error("Failed to create or bind socket to default port (%d), due to: %s", port, e);
            return;
        }

        Timer connCleanupTimer = new Timer();
        connCleanupTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ArrayList<Thread> connsToDelete = new ArrayList<>();
                for (int i = 0; i < connectionHandles.size(); i++) {
                    Thread handle = connectionHandles.get(i);
                    if (handle.isInterrupted() || !handle.isAlive()) {
                        connsToDelete.add(handle);
                    }
                }
                for (Thread t : connsToDelete) {
                    connectionHandles.remove(t);
                }
            }
        }, 60 * 1000); // Every minute, cleanup arraylist of thread handles.

        while (!Thread.currentThread().isInterrupted()) {
            Socket conn = null;
            try {
                conn = socket.accept();
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                error("Failed to accept new connection due to: %s", e);
            }

            Thread threadHandle = new Thread(new AuthServerConnectionHandler(conn, Thread.currentThread()));
            threadHandle.start();
            connectionHandles.add(threadHandle);
        }

        connCleanupTimer.cancel();
        try {
            socket.close();
        } catch (IOException e) {
            error("Failed to close socket due to: %s", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * Loads the port from the file port.info
     *
     * @return the port stored in port.info, or 1256 in case no such file exists or it is empty
     */
    private int loadPort() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("./port.info")));
            String portStr = reader.readLine();

            return Integer.parseInt(portStr);
        } catch (FileNotFoundException e) {
            error("No file port.info, using default port (%d)", DEFAULT_PORT_AUTH_SERVER);
        } catch (IOException e) {
            error("No data in port.info, using default port (%d)", DEFAULT_PORT_AUTH_SERVER);
        } catch (NumberFormatException e) {
            error("Invalid port number in port.info, using default port (%d)", DEFAULT_PORT_AUTH_SERVER);
        }
        return DEFAULT_PORT_AUTH_SERVER;
    }

}
