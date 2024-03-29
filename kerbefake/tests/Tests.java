package kerbefake.tests;

import kerbefake.common.entities.*;
import kerbefake.common.errors.InvalidHexStringException;
import kerbefake.common.errors.InvalidMessageException;
import kerbefake.auth_server.entities.requests.get_sym_key.GetSymmetricKeyRequest;
import kerbefake.auth_server.entities.requests.get_sym_key.GetSymmetricKeyRequestBody;
import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequest;
import kerbefake.auth_server.entities.requests.register_client.RegisterClientRequestBody;
import kerbefake.auth_server.entities.responses.FailureResponse;
import kerbefake.auth_server.entities.responses.get_sym_key.GetSymmetricKeyResponse;
import kerbefake.auth_server.entities.responses.get_sym_key.GetSymmetricKeyResponseBody;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponse;
import kerbefake.auth_server.entities.responses.register_client.RegisterClientResponseBody;
import kerbefake.msg_server.entities.SendMessageRequest;
import kerbefake.msg_server.entities.SendMessageRequestBody;
import kerbefake.msg_server.entities.SubmitTicketRequest;
import kerbefake.msg_server.entities.SubmitTicketRequestBody;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static kerbefake.common.Logger.error;
import static kerbefake.common.Logger.info;
import static kerbefake.common.Utils.bytesToHexString;
import static kerbefake.common.Utils.hexStringToByteArray;
import static kerbefake.tests.TestUtils.*;

/**
 * A test class for client registration request - {@link RegisterClientRequest}
 */
@SuppressWarnings({"unused", "ConstantValue", "JavadocDeclaration"})
final class Tests {

    public static final String PASSWORD = "strongPassword123!";

    public static final String SERVER_ID = "21da1d0e32944e64944c6f864aa6b7b4";
    public static final String EXPECTED_TEST_FAILURE_STRING = "TEST - ❌ Failed";

    public static String CLIENT_ID = null;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException, InvalidHexStringException {
        info("TEST - ================ \uD83E\uDDEA Testing full correct flow \uD83E\uDDEA ===================");
        testFullCorrectFlow();
        info("TEST - Waiting 3 seconds to make sure all threads are dead.");
        Thread.sleep(3 * 1000);
        info("TEST - ================ \uD83E\uDDEA Testing expected failure duplicate registration \uD83E\uDDEA ===================");
        testDuplicateRegister();
        Thread.sleep(3 * 1000);
        info("TEST - ================ \uD83E\uDDEA Testing expected failure due to ticket being expired \uD83E\uDDEA ===================");
        testExpiredTicket();
    }

    private static void testExpiredTicket() throws NoSuchAlgorithmException, InterruptedException, IOException, InvalidHexStringException {
        SocketAndStreams result = getSocketsAndStreams();

        try {
            String clientId;
            if (CLIENT_ID == null) {
                clientId = registerClient(result.authServerOutputStream, result.authServerInputStream);
            } else {
                info("TEST - No need to register, client ID is provided.");
                clientId = CLIENT_ID;
            }

            if (clientId == null) {
                endTest(result.authServerSocket, result.authServerThreadHandle, result.msgServerThreadHandle);
                return;
            }

            GetSymmetricKeyResponse getSymKey = getSymKey(result.authServerOutputStream, result.authServerInputStream, clientId);
            if (getSymKey == null) {
                endTest(result.authServerSocket, result.authServerThreadHandle, result.msgServerThreadHandle);
                return;
            }

            result.authServerSocket.close();

            EncryptedKey encryptedKey = getSessionKey(getSymKey, clientId);
            Ticket ticket = ((GetSymmetricKeyResponseBody) getSymKey.getBody()).getTicket();

            info("Test - sleeping for 5 seconds");
            Thread.sleep(5 * 1000);
            try {
                submitTicketToMsgServer(result.msgServerOutputStream, result.msgServerInputStream, ticket, clientId, encryptedKey);
            } catch (InvalidHexStringException | RuntimeException e) {
                if (e.getMessage().equals(EXPECTED_TEST_FAILURE_STRING)) {
                    info("TEST - ✅ Test failed as expected");
                    return;
                }
            }
            info("TEST - ❌ Failed to raise error when submitting expired ticket to message server");
        } finally {
            if (!result.authServerSocket.isClosed())
                endTest(result.authServerSocket);
            endTest(result.msgServerSocket, result.authServerThreadHandle, result.msgServerThreadHandle);
        }

    }


    /**
     * Test to make sure we get a failure when trying to register when already registered.
     *
     * @throws IOException
     */
    private static void testDuplicateRegister() throws IOException, InterruptedException {
        info("TEST - ================ Expected failure from dup register ===================");
        info("TEST - Trying to register client with auth server.");

        SocketAndStreams socketsAndStreams = getSocketsAndStreams();
        try {
            registerClient(socketsAndStreams.authServerOutputStream, socketsAndStreams.authServerInputStream);
        } catch (InvalidHexStringException | RuntimeException e) {
            if (e.getMessage().equals(EXPECTED_TEST_FAILURE_STRING)) {
                info("TEST - ✅ Test failed as expected");
                return;
            }
        } finally {
            if (!socketsAndStreams.authServerSocket.isClosed())
                endTest(socketsAndStreams.authServerSocket);
            endTest(socketsAndStreams.msgServerSocket, socketsAndStreams.authServerThreadHandle, socketsAndStreams.msgServerThreadHandle);
        }
        info("TEST - ❌ Failed to raise error when performing duplicate register with auth server");

    }

    /**
     * Tests for a full correct flow where we go from registration to sending a message to the server
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static void testFullCorrectFlow() throws IOException, NoSuchAlgorithmException, InterruptedException, InvalidHexStringException {
        SocketAndStreams result = getSocketsAndStreams();

        try {
            String clientId;
            if (CLIENT_ID == null) {
                clientId = registerClient(result.authServerOutputStream, result.authServerInputStream);
            } else {
                info("TEST - No need to register, client ID is provided.");
                clientId = CLIENT_ID;
            }
            CLIENT_ID = clientId;

            if (clientId == null) {
                endTest(result.authServerSocket, result.authServerThreadHandle, result.msgServerThreadHandle);
                return;
            }

            GetSymmetricKeyResponse getSymKey = getSymKey(result.authServerOutputStream, result.authServerInputStream, clientId);
            if (getSymKey == null) {
                endTest(result.authServerSocket, result.authServerThreadHandle, result.msgServerThreadHandle);
                return;
            }

            result.authServerSocket.close();

            EncryptedKey encryptedKey = getSessionKey(getSymKey, clientId);
            Ticket ticket = ((GetSymmetricKeyResponseBody) getSymKey.getBody()).getTicket();

            submitTicketToMsgServer(result.msgServerOutputStream, result.msgServerInputStream, ticket, clientId, encryptedKey);

            sendMessageToMsgServer(result.msgServerOutputStream, result.msgServerInputStream, encryptedKey, clientId);
        } finally {
            if (!result.authServerSocket.isClosed())
                endTest(result.authServerSocket);
            endTest(result.msgServerSocket, result.authServerThreadHandle, result.msgServerThreadHandle);
        }

    }

    /**
     * Test to register a client to the auth server
     *
     * @param out - the output stream to use to send messages
     * @param in  - the input stream to use to read responses
     * @return A string corresponding to the client ID or null in case of an error
     * @throws IOException - in case of an IO error when communicating with the server
     */
    private static String registerClient(OutputStream out, InputStream in) throws IOException, InvalidHexStringException {
        info("TEST - ================ Submit Ticket to Message Server (1024) ===================");
        info("TEST - Trying to register client with auth server.");

        String randomId = bytesToHexString(new byte[16]);
        String name = "Ron Person\0";
        char[] password = "strongPassword123!\0".toCharArray();
        int payloadSize = name.length() + password.length;
        RegisterClientRequest request = new RegisterClientRequest(new ServerMessageHeader(randomId, (byte) 4, MessageCode.REGISTER_CLIENT, payloadSize), new RegisterClientRequestBody(name, password));
        RegisterClientResponse response = sendRequestAndGetResponse(out, in, request);
        if (response == null) {
            throw new RuntimeException("TEST - ❌ Failed to register client with auth server");
        }
        info("TEST - ✅ Successfully registered client with auth server");

        RegisterClientResponseBody responseBody = (RegisterClientResponseBody) response.getBody();
        return responseBody.getId();

    }

    private static GetSymmetricKeyResponse getSymKey(OutputStream out, InputStream in, String clientId) throws IOException, InvalidHexStringException {
        info("TEST - ================ Submit Ticket to Message Server (1027) ===================");
        info("TEST - Trying to get symmetric key from auth server.");
        byte[] nonce = new byte[8];
        new SecureRandom().nextBytes(nonce);
        GetSymmetricKeyRequestBody body = new GetSymmetricKeyRequestBody(SERVER_ID, nonce);
        GetSymmetricKeyRequest request = new GetSymmetricKeyRequest(
                new ServerMessageHeader(clientId, (byte) 4, MessageCode.REQUEST_SYMMETRIC_KEY, body.toLEByteArray().length),
                body
        );
        GetSymmetricKeyResponse response = sendRequestAndGetResponse(out, in, request);
        if (response == null) {
            throw new RuntimeException("TEST - ❌ Failed to get symmetric key and ticket from auth server");
        }
        info("TEST - ✅ Successfully got symmetric key and ticket from auth server");

        return response;
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
    private static void submitTicketToMsgServer(OutputStream out, InputStream in, Ticket ticket, String clientId, EncryptedKey key) throws IOException, InvalidHexStringException {
        info("TEST - ================ Submit Ticket to Message Server (1028) ===================");
        info("TEST - Submitting a ticket to the message server.");

        byte[] iv = getIv();
        Authenticator authenticator = new Authenticator(
                iv,
                hexStringToByteArray(clientId),
                hexStringToByteArray(SERVER_ID),
                ticket.getCreationTime()
        );
        authenticator.encrypt(key.getAesKey());

        SubmitTicketRequestBody body = new SubmitTicketRequestBody(authenticator, ticket);
        SubmitTicketRequest request = new SubmitTicketRequest(new ServerMessageHeader(clientId, (byte) 24, MessageCode.SUBMIT_TICKET, body.toLEByteArray().length), body);

        if (sendRequestAndGetResponse(out, in, request) == null) {
            throw new RuntimeException("TEST - ❌ Failed to submit ticket to message server");
        }
        info("TEST - ✅ Successfully submitted a ticket");
    }

    /**
     * Sends a message to the message server, must be done after establishing a session with the message server.
     *
     * @param out      - the output stream to use.
     * @param in       - the input stream to use.
     * @param key      - the encrypted key containing the session key to use for encryption
     * @param clientId - the client ID to use
     * @throws IOException in case of a failure to send the request or get the response
     */
    private static void sendMessageToMsgServer(OutputStream out, InputStream in, EncryptedKey key, String clientId) throws IOException, InvalidHexStringException {
        info("TEST - ================ Send Message to Message Server ===================");
        info("TEST - Sending a message to the message server.");
        byte[] iv = getIv();
        String message = "Hello this is some random message very long hahahah 1231 123 123 40n3 to wef";

        SendMessageRequestBody body = new SendMessageRequestBody(iv, message);
        body.encrypt(key.getAesKey());
        SendMessageRequest request = new SendMessageRequest(new ServerMessageHeader(clientId, (byte) 24, MessageCode.SEND_MESSAGE, body.toLEByteArray().length), body);
        if (sendRequestAndGetResponse(out, in, request) == null) {
            throw new RuntimeException("TEST - ❌ Failed to send message or get response");
        }
        info("TEST - ✅ Successfully sent a message");

    }

    /**
     * Gets a random 16 byte IV
     *
     * @return a 16 byte IV
     */
    private static byte[] getIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Ends the testing
     *
     * @param socket        - the socket to close
     * @param threadHandles - the thread running the auth server
     * @throws IOException - in case of failure to interrupt the auth server or failure to close the socket
     */
    private static void endTest(Socket socket, Thread... threadHandles) throws IOException, InterruptedException {
        socket.close();
        for (Thread t : threadHandles) {
            t.interrupt();
            t.join();
        }
    }

    private static <T extends ServerMessage> T sendRequestAndGetResponse(OutputStream out, InputStream in, ServerMessage req) throws IOException, InvalidHexStringException {
        out.write(req.toLEByteArray());
        ServerMessage response = null;
        try {
            response = ServerMessage.parse(in, true);
            if (response instanceof FailureResponse) {
                throw new RuntimeException("TEST - ❌ Failed");
            }
            return (T) response;
        } catch (InvalidMessageException e) {
            e.printStackTrace();
            error("TEST - ❌ Failed to parse response due to: %s", e);
        }
        return null;
    }

    /**
     * Starts the servers and opens the needed streams and sockets.
     *
     * @return an object containing all the streams and sockets needed for testing
     * @throws IOException
     */
    private static SocketAndStreams getSocketsAndStreams() throws IOException {
        Thread authServerThreadHandle = startAuthServer();
        Thread msgServerThreadHandle = startMessageServer();

        Socket authServerSocket = new Socket("127.0.0.1", 1256);
        OutputStream authServerOutputStream = authServerSocket.getOutputStream();
        InputStream authServerInputStream = authServerSocket.getInputStream();
        Socket msgServerSocket = new Socket("127.0.0.1", 1235);
        OutputStream msgServerOutputStream = msgServerSocket.getOutputStream();
        InputStream msgServerInputStream = msgServerSocket.getInputStream();
        SocketAndStreams result = new SocketAndStreams(authServerThreadHandle, msgServerThreadHandle, authServerSocket, authServerOutputStream, authServerInputStream, msgServerSocket, msgServerOutputStream, msgServerInputStream);
        return result;
    }

    private static class SocketAndStreams {
        public final Thread authServerThreadHandle;
        public final Thread msgServerThreadHandle;
        public final Socket authServerSocket;
        public final OutputStream authServerOutputStream;
        public final InputStream authServerInputStream;
        public final Socket msgServerSocket;
        public final OutputStream msgServerOutputStream;
        public final InputStream msgServerInputStream;

        public SocketAndStreams(Thread authServerThreadHandle, Thread msgServerThreadHandle, Socket authServerSocket, OutputStream authServerOutputStream, InputStream authServerInputStream, Socket msgServerSocket, OutputStream msgServerOutputStream, InputStream msgServerInputStream) {
            this.authServerThreadHandle = authServerThreadHandle;
            this.msgServerThreadHandle = msgServerThreadHandle;
            this.authServerSocket = authServerSocket;
            this.authServerOutputStream = authServerOutputStream;
            this.authServerInputStream = authServerInputStream;
            this.msgServerSocket = msgServerSocket;
            this.msgServerOutputStream = msgServerOutputStream;
            this.msgServerInputStream = msgServerInputStream;
        }
    }
}
