package kerbefake;

import kerbefake.auth_server.AuthServer;
import kerbefake.client.Client;
import kerbefake.common.Constants;
import kerbefake.msg_server.MessageServer;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static kerbefake.common.Logger.commonLogger;

public class Main {

    private static Scanner userInputScanner = new Scanner(System.in);
    private static final String menu = "Please select mode of operation:\n1) Client mode\n2) Auth Server\n3) Messaging Server";

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                String name = t.getName();
                if (name.equals("AuthServerThread") || name.equals("MessageServerThread"))
                    t.interrupt();
            }
        }));


        boolean singleOperationMode = getIsSingleOperationMode();

        try {
            if (!singleOperationMode) {
                multipleModeExecution();
                return;
            }

            singleModeExecution();
        } catch (NoSuchElementException e) {
            // We caught a CTRL + C, ignore and let threads terminate
            commonLogger.info("CTRL+C Detected, exiting");
            commonLogger.error(e);
        }
    }

    private static void singleModeExecution() {
        commonLogger.info("Using single operation mode");

        commonLogger.print(menu);
        System.out.print("> ");
        int modeSelection = userInputScanner.nextInt();
        switch (modeSelection) {
            case Constants.MODE_CLIENT:
                // Closing scanner, so we don't get a duplicate read for some reason.
                userInputScanner = null;
                new Client(true).run();
                return;
            case Constants.MODE_AUTH:
                new AuthServer(true).start();
                return;
            case Constants.MODE_SERVER:
                try {
                    new MessageServer(true).start();
                } catch (IOException e) {
                    commonLogger.error("Failed to start message server due to: %s", e);
                }
                return;
            default:
                commonLogger.info("Provided invalid input, exiting");
        }
    }

    /**
     * Allows the execution of multiple modes via a single console.
     */
    private static void multipleModeExecution() {
        commonLogger.info("Using multi operation mode");
        Thread authServerThread = null, msgServerThread = null;
        int modeSelection;
        try {
            do {
                commonLogger.print(menu);
                modeSelection = userInputScanner.nextInt();
                switch (modeSelection) {
                    case Constants.MODE_CLIENT:
                        // Closing scanner so we don't get a duplicate read for some reason.
                        userInputScanner = null;
                        new Client().run();
                        return;
                    case Constants.MODE_AUTH:
                        if (authServerThread != null) {
                            commonLogger.error("Auth server is already running.");
                            break;
                        }
                        AuthServer authServer = new AuthServer();
                        authServerThread = new Thread(authServer::start);
                        authServerThread.setName("AuthServerThread");
                        authServerThread.start();
                        break;
                    case Constants.MODE_SERVER:
                        if (msgServerThread != null) {
                            commonLogger.error("Message server is already running.");
                            break;
                        }
                        try {
                            MessageServer msgServer = new MessageServer();
                            msgServerThread = new Thread(msgServer::start);
                            msgServerThread.setName("MessageServerThread");
                            msgServerThread.start();
                        } catch (IOException e) {
                            commonLogger.error("Failed to start message server due to: %s", e);
                            return;
                        }
                        break;
                    default:
                        commonLogger.info("Provided invalid input, exiting");
                        return;
                }
            } while (true);
        } finally {
            if (authServerThread != null)
                authServerThread.interrupt();
            if (msgServerThread != null)
                msgServerThread.interrupt();
        }
    }

    /**
     * Checks if we want to run a single mode of operation in the execution console or if we want to have multiple executions in a single console.
     *
     * @return the user's decision.
     */
    private static boolean getIsSingleOperationMode() {
        commonLogger.print("Would you like to run multiple servers via single execution or one mode per execution?[Y/n]");
        String[] positiveResponses = {"Y", "y", "yes", "YES", "Yes"};
        String response = userInputScanner.next() + userInputScanner.nextLine();

        for (String positiveResponse : positiveResponses) {
            if (response.equals(positiveResponse))
                return true;
        }
        return false;
    }
}
