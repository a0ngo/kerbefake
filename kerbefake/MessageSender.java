package kerbefake;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.Key;
import kerbefake.client.ClientConnection;

public class MessageSender {
    private final ClientConnection clientConnection;
    private final byte[] sessionKey;

    public MessageSender(ClientConnection clientConnection, byte[] sessionKey) {
        this.clientConnection = clientConnection;
        this.sessionKey = sessionKey;
    }

    public boolean sendMessage(String recipientId, String message) {
        try {
            byte[] encryptedMessage = encryptMessage(message);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(recipientId);
            dos.writeInt(encryptedMessage.length);
            dos.write(encryptedMessage);

            byte[] messagePacket = baos.toByteArray();
//            clientConnection.send(messagePacket);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            return false;
        }
    }

    private byte[] encryptMessage(String message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        Key aesKey = new SecretKeySpec(sessionKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(message.getBytes());
    }
}
