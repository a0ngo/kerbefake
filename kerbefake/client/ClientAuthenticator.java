package kerbefake.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientAuthenticator {
    private final ClientNetworkHandler networkHandler;
    public boolean isRegistered;
    private byte[] sessionKey;

    public ClientAuthenticator(ClientNetworkHandler networkHandler) {
        this.networkHandler = networkHandler;
        this.isRegistered = false;
    }

    public boolean register(String username, String password) {
        try {
            byte[] passwordHash = hashPassword(password);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(username);
            dos.writeInt(passwordHash.length);
            dos.write(passwordHash);
            byte[] registrationMessage = baos.toByteArray();

            networkHandler.sendMessageToServer(registrationMessage);

            byte[] response = networkHandler.receiveMessageFromServer();
            if (response[0] == 1) { // successful registration
                isRegistered = true;
                sessionKey = extractSessionKey(response);
            }
            return isRegistered;
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    private byte[] hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(password.getBytes());
    }

    private byte[] extractSessionKey(byte[] response) {
        byte[] sessionKey = new byte[response.length - 1];
        System.arraycopy(response, 1, sessionKey, 0, response.length - 1);
        return sessionKey;
    }
}
