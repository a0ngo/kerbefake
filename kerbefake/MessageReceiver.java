package kerbefake;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import kerbefake.client.ClientConnection;

public class MessageReceiver implements Runnable {
    private final ClientConnection clientConnection;
    private final byte[] sessionKey;

    public MessageReceiver(ClientConnection clientConnection, byte[] sessionKey) {
        this.clientConnection = clientConnection;
        this.sessionKey = sessionKey;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Wait 
//                byte[] encryptedMessage = clientConnection.receiveMessageFromServer();
//                // Decrypt
//                String message = decryptMessage(encryptedMessage);
//                // Display
//                System.out.println("Received message: " + message);
            }
        } catch (Exception e) {
            System.err.println("Error while receiving or decrypting message: " + e.getMessage());
        }
    }

    private String decryptMessage(byte[] encryptedMessage) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        Key aesKey = new SecretKeySpec(sessionKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
        return new String(decryptedBytes);
    }

    public void startListening() {
        Thread listenerThread = new Thread(this);
        listenerThread.start();
    }
}
