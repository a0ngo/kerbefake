package kerbefake.tests;

import kerbefake.AuthServer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import static kerbefake.Logger.info;

/**
 * General utilities for testing purposes.
 */
public final class TestUtils {

    public static void startAuthServer() {
        AuthServer server = new AuthServer();
        new Thread(server::start).start();
        try {
            info("Sleeping for 10 seconds to ensure server startup.");
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
