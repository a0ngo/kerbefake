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

    public static void main(String[] args) {
        int modeSelection;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                String name = t.getName();
                if (name.equals("AuthServerThread") || name.equals("MessageServerThread"))
                    t.interrupt();
            }

        }));

        Thread authServerThread = null, msgServerThread = null;
        Scanner userInputScanner = new Scanner(System.in);
        try {
            do {
                commonLogger.print("Please select mode of operation:\n1) Client mode\n2) Auth Server\n3) Messaging Server");
                modeSelection = userInputScanner.nextInt();
                switch (modeSelection) {
                    case Constants.MODE_CLIENT:
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
        } catch (NoSuchElementException e) {
            // We caught a CTRL + C, ignore and let threads terminate
            commonLogger.info("CTRL+C Detected, exiting");
        } finally {
            if (authServerThread != null)
                authServerThread.interrupt();
            if (msgServerThread != null)
                msgServerThread.interrupt();
        }

    }
}
