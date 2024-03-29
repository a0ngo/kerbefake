package kerbefake.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static kerbefake.Logger.error;
import static kerbefake.Logger.warn;

/**
 * This class is responsible for handling all the connections and sessions that a client creates.
 * This acts as sort of a cache
 */
public final class NetworkManager {

    private static NetworkManager instance;

    private Map<ServerType, ConnectionDetails> connections;

    private Timer terminationTimer;

    public static NetworkManager getInstance() {
        return instance == null ? new NetworkManager() : instance;
    }

    private NetworkManager() {
        instance = this;
        connections = new HashMap<>();
        terminationTimer = new Timer();
    }

    /**
     * Opens a connection to a specific server and stores in memory for future handling and caching.
     *
     * @param type          - the server type that we want to open a connection to.
     * @param ip            - the ip of the server
     * @param port          - the port of the server
     * @param timeTillClose - how long in seconds until we close the connection.
     * @return - a {@link ClientConnection} for the connection to the server.
     */
    public ClientConnection openConnection(ServerType type, String ip, int port, int timeTillClose) {
        // I don't like to put so many if statements nested, but it doesn't make sense to pull this to a separate method.
        if (connections.containsKey(type)) {
            ConnectionDetails existingConnectionDetails = connections.get(type);
            if (existingConnectionDetails == null) {
                connections.remove(type);
            } else {
                ClientConnection existingConnection = existingConnectionDetails.getConnection();
                if (existingConnection == null || !existingConnection.isOpen()) {
                    existingConnectionDetails.terminate();
                    connections.remove(type);
                } else if (existingConnection.getServerAddress().equals(String.format("%s:%d", ip, port))) {
                    warn("Tried to open a connection to a server that we already have an open connection to (%s:%d) using existing connection.", ip, port);
                    return existingConnection;
                } else {
                    // We already have a connection open to some server which is not the same ip and port, we force the closure of it and open a new one.
                    existingConnectionDetails.terminate();
                    connections.remove(type);
                }
            }
        }

        ClientConnection connection = new ClientConnection(ip, port);
        boolean opened = connection.open();

        if (!opened) {
            error("Failed to open a connection to %s:%d", ip, port);
            return null;
        }

        TimerTask terminateConnectionTask = new TimerTask() {
            @Override
            public void run() {
                connection.close();
            }
        };
        terminationTimer.schedule(terminateConnectionTask, timeTillClose);

        ConnectionDetails details = new ConnectionDetails(terminateConnectionTask, connection);
        this.connections.put(type, details);
        return connection;
    }

    /**
     * An enum that contains the types of servers we can connect to.
     */
    protected enum ServerType {

        AUTH,
        MESSAGE;
    }

    /**
     * A class the represents connection details to a server.
     */
    private static final class ConnectionDetails {

        private TimerTask terminationTask;

        private ClientConnection connection;

        // TODO: Add session


        public ConnectionDetails(TimerTask terminationTask, ClientConnection connection) {
            this.terminationTask = terminationTask;
            this.connection = connection;
        }

        public TimerTask getTerminationTask() {
            return terminationTask;
        }

        public void setTerminationTask(TimerTask terminationTask) {
            this.terminationTask = terminationTask;
        }

        public ClientConnection getConnection() {
            return connection;
        }

        public void setConnection(ClientConnection connection) {
            this.connection = connection;
        }

        /**
         * Terminates this connection and removes termination timer.
         */
        public void terminate() {
            if (connection != null) {
                connection.close();
                ;
            }
            if (terminationTask != null) {
                terminationTask.cancel();
            }
        }
    }
}
