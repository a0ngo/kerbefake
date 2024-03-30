package kerbefake;

import kerbefake.auth_server.AuthServer;
import kerbefake.client.Client;
import kerbefake.common.Constants;
import kerbefake.msg_server.MessageServer;

import java.io.IOException;
import java.util.Scanner;

import static kerbefake.common.Logger.commonLogger;

public class Main {

    public static void main(String[] args) {
        int modeSelection = -1;

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
                        AuthServer authServer = new AuthServer();
                        authServerThread = new Thread(authServer::start);
                        authServerThread.start();
                        break;
                    case Constants.MODE_SERVER:
                        try {
                            MessageServer msgServer = new MessageServer();
                            msgServerThread = new Thread(msgServer::start);
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
}
