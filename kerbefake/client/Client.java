package kerbefake.client;

import java.io.IOException;
import java.util.Scanner;

import kerbefake.errors.InvalidClientConfigException;
import kerbefake.errors.InvalidHexStringException;
import kerbefake.errors.InvalidMessageException;
import kerbefake.models.MessageCode;
import kerbefake.models.ServerMessage;
import kerbefake.models.ServerMessageHeader;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequest;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequestBody;
import kerbefake.models.auth_server.responses.FailureResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponseBody;

import static kerbefake.Constants.ClientConstants.*;
import static kerbefake.Constants.ID_LENGTH;
import static kerbefake.Constants.ResponseCodes.REGISTER_CLIENT_FAILURE_CODE;
import static kerbefake.Constants.ResponseCodes.UNKNOWN_FAILURE_CODE;
import static kerbefake.Constants.SERVER_VERSION;
import static kerbefake.Logger.*;
import static kerbefake.client.Client.ClientState.AFTER_REGISTER;
import static kerbefake.client.Client.ClientState.BEFORE_REGISTER;

public class Client implements Runnable {

    /**
     * A regex for IP - https://stackoverflow.com/a/36760050
     */
    private static final String IP_REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

    /**
     * A very disgusting regex for an IP + port combination.
     */
    private static final String IP_AND_PORT_REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}:([1-9]|[1-9][0-9]|[1-9][0-9][0-9]|[1-9][0-9][0-9][0-9]|[1-5][0-9][0-9][0-9][0-9]|6[0-4][0-9][0-9][0-9]|65[0-4][0-9][0-9]|655[0-2][0-9]|6553[0-5])$";

    private static final Scanner inputHandler = new Scanner(System.in);
    private ClientConfig clientConfig;
    private NetworkManager networkManager;

    private ClientState clientState;

    private ClientAuthenticator authenticator;

    public Client() {
        try {
            clientConfig = ClientConfig.load();
            clientState = AFTER_REGISTER;
        } catch (InvalidClientConfigException e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                e.printStackTrace();
                error("Client configuration is invalid due to: %s", e.getMessage());
                System.exit(1);
                return;
            }
            // File not found, this could be because the client simply never connected.
            warn("No me.info file found, if this is the first run please ignore this message.\nIf you registered in the past connection to the server might not be possible, please ask your server admin to reset your client connection by name.");
            clientState = BEFORE_REGISTER;
            clientConfig = ClientConfig.createEmpty();
        }
        networkManager = NetworkManager.getInstance();
    }


    private int getOperationToPerform() {
        String message;
        switch (clientState) {
            case BEFORE_REGISTER:
                message = MENU_PRE_REGISTER;
                break;
            case AFTER_REGISTER:
                message = MENU_POST_REGISTER;
                break;
            case AFTER_TICKET:
                message = MENU_POST_TICKET;
                break;
            default:
                error("Unknown client state, trying to recover.");
                // TODO: Add attempted recover from client state.
                System.exit(1);
                return -1;
        }
        return promptInt(message, 1, 2);
    }

    /**
     * Prompts the user for a string - this is non-confidential so the output will be printed on screen.
     *
     * @param message  - the message to show the user.
     * @param notEmpty - whether we accept an empty string
     * @return the string the user provided.
     */
    private String promptString(String message, boolean notEmpty) {
        String response = (String) prompt(message, String.class);
        while (!notEmpty && (response == null || response.isEmpty())) {
            error("Please provide a non-empty string.");
            response = inputHandler.next();
        }

        return response;
    }

    /**
     * Prompts the user for an integer
     *
     * @param message    - the message to show the user.
     * @param startRange - the start range of the accepted values, -1 if there is no range (also endRange must be -1 in such a case)
     * @param endRange   - the end range of the accepted values, -1 if there is no range (also startRange must be -1 in such a case)
     * @return - the int provided by the user.
     */
    private int promptInt(String message, int startRange, int endRange) {

        int response = (int) prompt(message, Integer.class);
        if (startRange > endRange) {
            warn("Range provided is invalid (%d - %d), returning user response", startRange, endRange);
            return response;
        }
        while ((startRange != -1 && endRange != -1) && (response < startRange || response > endRange)) {
            error("Please provide an integer between %d and %d", startRange, endRange);
            response = inputHandler.nextInt();
        }

        return response;
    }

    /**
     * Prompts the user for an input
     *
     * @param message   - the message to show the user
     * @param valueType - the value type of the return we expect, this enforces types from the scanner.
     * @return int if {@code valueType} is {@code Integer.class} otherwise a string
     */
    private Object prompt(String message, Class<?> valueType) {
        System.out.println(message);
        System.out.print("> ");
        if (valueType.equals(Integer.class)) {
            return inputHandler.nextInt();
        } else {
            if (!valueType.equals(String.class))
                error("Unknown expected input type %s, defaulting to string", valueType.getCanonicalName());
            return inputHandler.next();
        }
    }

    /**
     * Gets the password from the user and immediately hashes it. The value is stored only if we're in the pre-register state
     * in which case we need it for the registration operation.
     */
    private void getPasswordFromUser() {
        info("Please provide your password;");
        System.out.print("> ");
        char[] password = System.console().readPassword();
        if (clientState == BEFORE_REGISTER) {
            this.clientConfig.setPlainTextPassword(password);
        }
        this.clientConfig.setPassword(password);
    }

    private String getNameFromUser() {
        return promptString("Please provide your name;", true);
    }

    /**
     * Gets the server IP and port from the user.
     *
     * @param serverType     - the type of the server that we are connecting to, auth server or message server.
     * @param defaultAddress - the default address that will be used in case the user provides an empty string.
     * @return - the string that the user provided is such exists or the value of defaultAddress.
     */
    private String getServerAddress(String serverType, String defaultAddress) {
        // This do-while loop is meant to properly parse a provided IP:Port address provided by the user, if none is provided the above
        // is kept as default.
        do {
            String serverString = promptString(String.format("Please provide the %s server address (leave empty for %s)", serverType, defaultAddress), false);
            if (serverString != null && !serverString.isEmpty()) {
                if (serverString.matches(IP_REGEX)) {
                    int port = promptInt("Server provided without port, please provide the port used.", 1, 65535);
                    return String.format("%s:%d", serverString, port);
                } else if (serverString.matches(IP_AND_PORT_REGEX)) {
                    return serverString;
                } else {
                    continue;
                }
            }
            break;
        } while (true);
        return defaultAddress;
    }

    /**
     * Since each state has a single operation to perform, given a state value we know which operation we will perform and what type of result we expect.
     *
     * @return the server message that is the response we get from the server we communicate with.
     */
    private boolean performStateOperation() {
        switch (clientState) {
            case BEFORE_REGISTER:
                return registerToServer();
            case AFTER_REGISTER:
//                return getSymmetricKeyFromAuthServer();
            case AFTER_TICKET:
//                return sendMessageToMessageServer();
            default:
                //TODO: Add attempt to recover.
                return false;
        }
    }

    private boolean registerToServer() {
        final String defaultAddress = "127.0.0.1:1256";
        String fullServerAddress = getServerAddress("auth", defaultAddress);
        String serverIp = "127.0.0.1";
        int port = 1256;
        if (!fullServerAddress.equals(defaultAddress)) {
            String[] addressComponents = fullServerAddress.split(":");
            serverIp = addressComponents[0];
            port = Integer.parseInt(addressComponents[1]);
        }

        // Within 5 minute before this connection will be closed automatically
        ClientConnection authServerConn = networkManager.openConnection(NetworkManager.ServerType.AUTH, serverIp, port, 300);

        String name = getNameFromUser();

        RegisterClientRequestBody registerClientRequestBody = new RegisterClientRequestBody(name, this.clientConfig.getPlainTextPassword());
        ServerMessageHeader registerClentHeader = new ServerMessageHeader(SERVER_VERSION, MessageCode.REGISTER_CLIENT, registerClientRequestBody.toLEByteArray().length);
        RegisterClientRequest registerClientRequest = new RegisterClientRequest(registerClentHeader, registerClientRequestBody);

        try {
            ServerMessage response = authServerConn.send(registerClientRequest);
            if (response instanceof FailureResponse) {
                switch (response.getHeader().getMessageCode().getCode()) {
                    case UNKNOWN_FAILURE_CODE:
                        error(REQUEST_FAILED);
                        return false;
                    case REGISTER_CLIENT_FAILURE_CODE:
                        error("Failed to register client, you might already be registered, in that case please ask the admin to reset your user and delete the me.info file.");
                        return false;
                    default:
                        error("Received unknown response code: %d - can't proceed.", response.getHeader().getMessageCode().getCode());
                        return false;
                }
            }

            if (!(response instanceof RegisterClientResponse)) {
                error("Received unknown response, can't proceed, response is of type %s, printing response:\n %s", response.getClass().getCanonicalName(), response.toString());
                return false;
            }

            if (response.getBody() == null) {
                error("Received response but missing response body, can't proceed, please contact server admin.");
                return false;
            }

            RegisterClientResponseBody responseBody = (RegisterClientResponseBody) response.getBody();
            String clientId = responseBody.getId();

            if (clientId == null || clientId.length() != ID_LENGTH) {
                error("Invalid client ID received in the response, can't proceed, received: %s", clientId);
                return false;
            }

            this.clientConfig.setClientIdHex(clientId);
            this.clientConfig.clearPassword();
            return true;
        } catch (IOException | InvalidHexStringException e) {
            e.printStackTrace();
            if (e instanceof InvalidHexStringException) {
                // This can only happen if some hex string is invalid, but this is unlikely at this stage, we will print a message and fail regardless.
                error("Failed to encode or decode some part of the message sent or received from the server.");
                return false;
            }
            error("Failed to send a request to the auth server due to: %s.", e.getMessage());
        } catch (InvalidMessageException e) {
            e.printStackTrace();
            error("Failed to decode message from server due to: %s\nPlease forward this to your server admin.", e.getMessage());
        }
        return false;
    }

    @Override
    public void run() {
        getPasswordFromUser();
        while (!Thread.currentThread().isInterrupted()) {
            int operation = getOperationToPerform();

            // Operation 2 is always exit, since we work in a sequential order, meaning you must first register,
            // then get a ticket, then submit the ticket and send a message to the message server (combined into one as per protocol spec).
            if (operation == 2) {
                info("Exiting.");
                break;
            }

            performStateOperation();


//            int choice = scanner.nextInt();
//            switch (choice) {
//                case 1:
//                    clientMain.registerClient();
//                    break;
//                case 2:
//                    clientMain.sendMessage();
//                    break;
//                case 3:
//                    System.out.println("Exiting client.");
//                    System.exit(0);
//                default:
//                    System.out.println("Invalid choice. Please select a valid option.");
//            }
        }
    }

    /**
     * An enum to indicate which state our client is in.
     */
    enum ClientState {
        /**
         * We have not registered to the auth server yet.
         */
        BEFORE_REGISTER,
        /**
         * We have registered but have yet to obtain to a ticket and a session key to connect to the message server.
         */
        AFTER_REGISTER,
        /**
         * We have registered, have a client ID and our session key in memory and are ready to send a message to the message server.
         */
        AFTER_TICKET;
    }
}
