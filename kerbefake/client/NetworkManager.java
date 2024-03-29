package kerbefake.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static kerbefake.common.Logger.error;
import static kerbefake.common.Logger.warn;
import static kerbefake.client.UserInputOutputHandler.getServerAddress;

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
     * @see #openConnectionToUserProvidedServer(ServerType, String, int, int)
     */
    public ClientConnection openConnectionToUserProvidedServer(ServerType type, String defaultIp, int defaultPort) {
        return openConnection(type, defaultIp, defaultPort, 3000);
    }

    /**
     * Queries the user for an IP and port to connect to, following which tries to open a connection based off the server type provided.
     *
     * @param type          - the server type.
     * @param defaultIp     - the default IP for this server.
     * @param defaultPort   - the default port for this server.
     * @param timeTillClose - the time in seconds until the connection is closed automatically.
     * @return - A {@link ClientConnection} client connection for the connection to the server.
     */
    public ClientConnection openConnectionToUserProvidedServer(ServerType type, String defaultIp, int defaultPort, int timeTillClose) {
        String defaultAddress = String.format("%s:%d", defaultIp, defaultPort);
        String fullServerAddress = getServerAddress(type == ServerType.AUTH ? "auth" : "message", defaultAddress);
        String serverIp = defaultIp;
        int port = defaultPort;
        if (!fullServerAddress.equals(defaultAddress)) {
            String[] addressComponents = fullServerAddress.split(":");
            serverIp = addressComponents[0];
            port = Integer.parseInt(addressComponents[1]);
        }
        return openConnection(type, serverIp, port, timeTillClose);
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
        terminationTimer.schedule(terminateConnectionTask, timeTillClose * 1000L);

        ConnectionDetails details = new ConnectionDetails(terminateConnectionTask, connection, timeTillClose);
        this.connections.put(type, details);
        return connection;
    }

    /**
     * Tries to get an existing connection for a given server type
     *
     * @param serverType - the server type to check for
     * @return a {@link ClientConnection} for the server type if such exists, null otherwise.
     */
    public ClientConnection getConnectionForServer(ServerType serverType) {
        ConnectionDetails connDetails = connections.get(serverType);
        if (connDetails == null) {
            return null;
        }

        ClientConnection conn = connDetails.connection;
        if (!conn.isOpen()) {
            error("Connection was closed, will try to re-open");
            connections.remove(serverType);
            String[] addressComponents = conn.getServerAddress().split(":");
            String ip = addressComponents[0];
            int port = Integer.parseInt(addressComponents[1]);
            // 5 minute till termination.
            return openConnection(serverType, ip, port, 300);
        }

        connDetails.getTerminationTask().cancel();
        terminationTimer.schedule(connDetails.getTerminationTask(), connDetails.getTimeTillTermination());

        return conn;
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

        private int timeTillTermination;
        // TODO: Add session


        public ConnectionDetails(TimerTask terminationTask, ClientConnection connection, int timeTillTermination) {
            this.terminationTask = terminationTask;
            this.connection = connection;
            this.timeTillTermination = timeTillTermination;
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

        public int getTimeTillTermination() {
            return timeTillTermination;
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
