package kerbefake.client;

import java.util.Arrays;
import java.util.Scanner;

import kerbefake.client.operations.RegisterOperation;
import kerbefake.errors.InvalidClientConfigException;

import static kerbefake.Constants.ClientConstants.*;
import static kerbefake.Constants.ID_LENGTH;
import static kerbefake.Logger.*;
import static kerbefake.client.Client.ClientState.AFTER_REGISTER;
import static kerbefake.client.Client.ClientState.BEFORE_REGISTER;
import static kerbefake.client.UserInputOutputHandler.*;

public class Client implements Runnable {

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

        String clientId = new RegisterOperation(authServerConn, this.clientConfig.getPlainTextPassword()).perform();
        if (clientId == null || clientId.length() != ID_LENGTH) {
            error("Register operation failed.");
            return false;
        }

        this.clientConfig.setClientIdHex(clientId);
        this.clientConfig.clearPassword();
        return true;
    }

    @Override
    public void run() {
        char[] password = getPasswordFromUser();
        this.clientConfig.setPassword(password);
        if (clientState == BEFORE_REGISTER) {
            this.clientConfig.setPlainTextPassword(password);
        } else {
            Arrays.fill(password, (char) 0);
        }

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
