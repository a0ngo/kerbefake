package kerbefake.tests;

import kerbefake.errors.InvalidMessageException;
import kerbefake.models.*;
import kerbefake.models.auth_server.requests.get_sym_key.GetSymmetricKeyRequest;
import kerbefake.models.auth_server.requests.get_sym_key.GetSymmetricKeyRequestBody;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequest;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequestBody;
import kerbefake.models.auth_server.responses.get_sym_key.GetSymmetricKeyResponse;
import kerbefake.models.auth_server.responses.get_sym_key.GetSymmetricKeyResponseBody;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponseBody;
import kerbefake.models.msg_server.requests.SendMessageRequest;
import kerbefake.models.msg_server.requests.SendMessageRequestBody;
import kerbefake.models.msg_server.requests.SubmitTicketRequest;
import kerbefake.models.msg_server.requests.SubmitTicketRequestBody;
import kerbefake.models.EmptyResponse;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static kerbefake.Logger.error;
import static kerbefake.Logger.info;
import static kerbefake.Utils.bytesToHexString;
import static kerbefake.Utils.hexStringToByteArray;
import static kerbefake.tests.TestUtils.*;

/**
 * A test class for client registration request - {@link RegisterClientRequest}
 */
public final class Tests {

    public static final String PASSWORD = "strongPassword123!";

    public static final String SERVER_ID = "21da1d0e32944e64944c6f864aa6b7b4";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        info("TEST - Starting test for client registration request.");
        Thread authServerThreadHandle = startAuthServer();
        Thread msgServerThreadHandle = startMessageServer();

        info("TEST - Start registering");

        Socket authServerSocket = new Socket("127.0.0.1", 1256);
        Socket msgServerSocket = new Socket("127.0.0.1", 1235);
        OutputStream authServerOutputStream = authServerSocket.getOutputStream();
        InputStream authServerInputStream = authServerSocket.getInputStream();
        OutputStream msgServerOutputStream = msgServerSocket.getOutputStream();
        InputStream msgServerInputStream = msgServerSocket.getInputStream();

        String clientId = registerClient(authServerOutputStream, authServerInputStream);
        if (clientId == null) {
            error("TEST - Failed to register.");
            endTest(authServerSocket, authServerThreadHandle, msgServerThreadHandle);
            return;
        }

        GetSymmetricKeyResponse getSymKey = getSymKey(authServerOutputStream, authServerInputStream, clientId);
        if (getSymKey == null) {
            error("TEST - Failed to get symmetric key");
            endTest(authServerSocket, authServerThreadHandle, msgServerThreadHandle);
            return;
        }

        EncryptedKey encryptedKey = getSessionKey(getSymKey, clientId);
        Ticket ticket = ((GetSymmetricKeyResponseBody) getSymKey.getBody()).getTicket();
        info("TEST - Session key is: %s", bytesToHexString(encryptedKey.getAesKey()));

        submitTicketToMsgServer(msgServerOutputStream, msgServerInputStream, ticket, clientId, encryptedKey);

        sendMessageToMsgServer(msgServerOutputStream, msgServerInputStream, encryptedKey, clientId);

        endTest(msgServerSocket, authServerThreadHandle, msgServerThreadHandle);
    }

    /**
     * Test to register a client to the auth server
     *
     * @param out - the output stream to use to send messages
     * @param in  - the input stream to use to read responses
     * @return A string corresponding to the client ID or null in case of an error
     * @throws IOException - in case of an IO error when communicating with the server
     */
    private static String registerClient(OutputStream out, InputStream in) throws IOException {
        info("TEST - Preparing data for request");
        String randomId = bytesToHexString(new byte[16]);
        String name = "Ron Person\0";
        String password = "strongPassword123!\0";
        int payloadSize = name.length() + password.length();
        ServerMessage message = new RegisterClientRequest(new ServerMessageHeader(randomId, (byte) 4, MessageCode.REGISTER_CLIENT, payloadSize), new RegisterClientRequestBody(name, password));
        out.write(message.toLEByteArray());
        try {
            ServerMessage response = ServerMessage.parse(in, true);
            RegisterClientResponse registerRes = (RegisterClientResponse) response;
            info("TEST - Client id: " + registerRes.getBody().toString());
            return ((RegisterClientResponseBody) registerRes.getBody()).getId();
        } catch (InvalidMessageException e) {
            e.printStackTrace();
            error("Failed to parse response due to: %s", e);
        }

        return null;
    }

    private static GetSymmetricKeyResponse getSymKey(OutputStream out, InputStream in, String clientId) throws IOException {
        byte[] nonce = new byte[8];
        SecureRandom srand = new SecureRandom();
        srand.nextBytes(nonce);
        GetSymmetricKeyRequestBody body = new GetSymmetricKeyRequestBody(SERVER_ID, nonce);
        ServerMessage message = new GetSymmetricKeyRequest(
                new ServerMessageHeader(clientId, (byte) 4, MessageCode.REQUEST_SYMMETRIC_KEY, body.toLEByteArray().length),
                body
        );
        out.write(message.toLEByteArray());
        try {
            ServerMessage response = ServerMessage.parse(in, true);
            GetSymmetricKeyResponse resp = (GetSymmetricKeyResponse) response;
            info("TEST - Get symmetric key response: " + resp.getBody().toString());
            return resp;
        } catch (InvalidMessageException e) {
            e.printStackTrace();
            error("Failed to parse response due to: %s", e);
        }

        return null;
    }

    /**
     * Gets the session key from the GetSymmetricKeyResponse from the server.
     *
     * @param getSymKeyResp - the response from the server for a symmetric key
     * @param clientId      - the client ID to confirm it's our request
     * @return The {@link EncryptedKey} object after decryption
     * @throws NoSuchAlgorithmException - won't happen, unless a computer doesn't have SHA-256 installed
     */
    private static EncryptedKey getSessionKey(GetSymmetricKeyResponse getSymKeyResp, String clientId) throws NoSuchAlgorithmException {
        GetSymmetricKeyResponseBody getSymKeyBody = (GetSymmetricKeyResponseBody) getSymKeyResp.getBody();
        EncryptedKey encKey = getSymKeyBody.getEncKey();

        assert clientId.equals(getSymKeyBody.getClientId());

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(PASSWORD.getBytes());

        encKey.decrypt(key);
        return encKey;
    }

    /**
     * Submits a ticket to the message server and confirms it was received.
     *
     * @param out      - the output stream to use to write the request
     * @param in       - the input stream to use to read the response
     * @param ticket   - the ticket to send
     * @param clientId - our client ID
     * @param key      - the key which holds our session key
     */
    private static void submitTicketToMsgServer(OutputStream out, InputStream in, Ticket ticket, String clientId, EncryptedKey key) throws IOException {
        byte[] iv = new byte[16];
        SecureRandom srand = new SecureRandom();
        srand.nextBytes(iv);

        Authenticator authenticator = new Authenticator(
                iv,
                hexStringToByteArray(clientId),
                hexStringToByteArray(SERVER_ID),
                ticket.getCreationTime()
        );

        authenticator.encrypt(key.getAesKey());

        SubmitTicketRequestBody body = new SubmitTicketRequestBody(authenticator, ticket);
        SubmitTicketRequest request = new SubmitTicketRequest(new ServerMessageHeader(clientId, (byte) 24, MessageCode.SUBMIT_TICKET, body.toLEByteArray().length), body);

        out.write(request.toLEByteArray());
        try {
            ServerMessage response = ServerMessage.parse(in, true);
            EmptyResponse resp = (EmptyResponse) response;
            info("TEST - Got submit ticket response.");
        } catch (InvalidMessageException e) {
            e.printStackTrace();
            error("Failed to parse response due to: %s", e);
        }

    }

    private static void sendMessageToMsgServer(OutputStream out, InputStream in, EncryptedKey key, String clientId) throws IOException {
        byte[] iv = new byte[16];
        SecureRandom srand = new SecureRandom();
        srand.nextBytes(iv);
        String message = "Hello this is some random message very long hahahah 1231 123 123 40n3 to wef";

        SendMessageRequestBody body = new SendMessageRequestBody(iv, message);
        body.encrypt(key.getAesKey());
        SendMessageRequest request = new SendMessageRequest(new ServerMessageHeader(clientId, (byte) 24, MessageCode.SEND_MESSAGE, body.toLEByteArray().length), body);

        out.write(request.toLEByteArray());
        try {
            ServerMessage response = ServerMessage.parse(in, true);
            EmptyResponse resp = (EmptyResponse) response;
            info("TEST - Got send message response.");
        } catch (InvalidMessageException e) {
            e.printStackTrace();
            error("Failed to parse response due to: %s", e);
        }

    }

    /**
     * Ends the testing
     *
     * @param socket        - the socket to close
     * @param threadHandles - the thread running the auth server
     * @throws IOException - in case of failure to interrupt the auth server or failure to close the socket
     */
    private static void endTest(Socket socket, Thread... threadHandles) throws IOException {
        socket.close();
        for (Thread t : threadHandles)
            t.interrupt();
    }

}
