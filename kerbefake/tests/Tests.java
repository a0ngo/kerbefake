package kerbefake.tests;

import kerbefake.errors.InvalidMessageException;
import kerbefake.models.auth_server.AuthServerMessage;
import kerbefake.models.auth_server.AuthServerMessageHeader;
import kerbefake.models.auth_server.MessageCode;
import kerbefake.models.auth_server.requests.get_sym_key.GetSymmetricKeyRequest;
import kerbefake.models.auth_server.requests.get_sym_key.GetSymmetricKeyRequestBody;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequest;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequestBody;
import kerbefake.models.auth_server.responses.get_sym_key.GetSymmetricKeyResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponseBody;

import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;

import static kerbefake.Logger.error;
import static kerbefake.Logger.info;
import static kerbefake.Utils.bytesToHexString;
import static kerbefake.tests.TestUtils.*;

/**
 * A test class for client registration request - {@link RegisterClientRequest}
 */
public final class Tests {

    public static void main(String[] args) throws IOException {
        info("TEST - Starting test for client registration request.");
        Thread threadHandle = startAuthServer();

        info("TEST - Start registering");

        Socket socket = new Socket("127.0.0.1", 1256);
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        String clientId = registerClient(out, socket.getInputStream());
        if (clientId == null) {
            error("TEST - Failed to register.");
            endTest(socket, threadHandle);
            return;
        }

        GetSymmetricKeyResponse getSymKey = getSymKey(out, in, clientId);

        endTest(socket, threadHandle);
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
        AuthServerMessage message = new RegisterClientRequest(new AuthServerMessageHeader(randomId, (byte) 4, MessageCode.REGISTER_CLIENT, payloadSize), new RegisterClientRequestBody(name, password));
        out.write(message.toLEByteArray());
        try {
            AuthServerMessage response = AuthServerMessage.parse(in, true);
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
        GetSymmetricKeyRequestBody body = new GetSymmetricKeyRequestBody("21da1d0e32944e64944c6f864aa6b7b4", nonce);
        AuthServerMessage message = new GetSymmetricKeyRequest(
                new AuthServerMessageHeader(clientId, (byte) 4, MessageCode.REQUEST_SYMMETRIC_KEY, body.toLEByteArray().length),
                body
        );
        out.write(message.toLEByteArray());
        try {
            AuthServerMessage response = AuthServerMessage.parse(in, true);
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
     * Ends the testing
     *
     * @param socket       - the socket to close
     * @param threadHandle - the thread running the auth server
     * @throws IOException - in case of failure to interrupt the auth server or failure to close the socket
     */
    private static void endTest(Socket socket, Thread threadHandle) throws IOException {
        socket.close();
        threadHandle.interrupt();
    }

}
