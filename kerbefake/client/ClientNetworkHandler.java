package kerbefake.client;

import java.io.*;
import java.net.Socket;

public class ClientNetworkHandler {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public ClientNetworkHandler(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            System.out.println("Connected to server at " + serverAddress + ":" + serverPort);
        } catch (IOException e) {
            System.err.println("Could not connect to server at " + serverAddress + ":" + serverPort);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void sendMessageToServer(byte[] message) throws IOException {
        out.writeInt(message.length);
        out.write(message);
        out.flush();
    }

    public byte[] receiveMessageFromServer() throws IOException {
        int messageLength = in.readInt();
        if (messageLength > 0) {
            byte[] message = new byte[messageLength];
            in.readFully(message);
            return message;
        }
        return new byte[0];
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
            System.out.println("Connection to server closed.");
        } catch (IOException e) {
            System.err.println("Error closing connection to server.");
            e.printStackTrace();
        }
    }
}
