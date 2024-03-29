package kerbefake.tests;

import kerbefake.auth_server.AuthServer;
import kerbefake.msg_server.MessageServer;

import java.io.IOException;
import java.util.Random;

import static kerbefake.common.Logger.info;

/**
 * General utilities for testing purposes.
 */
final class TestUtils {

    public static Thread startAuthServer() {
        AuthServer server = new AuthServer();
        Thread t = new Thread(server::start);
        t.start();
        try {
            info("Sleeping for 3 seconds to ensure server startup.");
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    public static Thread startMessageServer() throws IOException {
        MessageServer server = new MessageServer();
        Thread t = new Thread(server::start);
        t.start();
        try{
            info("Sleeping for 3 seconds to ensurer server startup.");
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    public static String generateRandomID() {
        String idChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_1234567890";
        StringBuilder idBuilder = new StringBuilder();
        Random rand = new Random();
        while (idBuilder.length() < 16) {
            int index = (int) (rand.nextFloat() * idChars.length());
            idBuilder.append(idChars.charAt(index));
        }
        return idBuilder.toString();
    }
}
