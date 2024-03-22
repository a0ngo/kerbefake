package kerbefake.tests;

import kerbefake.errors.InvalidMessageException;
import kerbefake.models.auth_server.AuthServerMessage;
import kerbefake.models.auth_server.AuthServerMessageHeader;
import kerbefake.models.auth_server.MessageCode;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequest;
import kerbefake.models.auth_server.requests.register_client.RegisterClientRequestBody;
import kerbefake.models.auth_server.responses.register_client.RegisterClientResponse;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static kerbefake.Logger.error;
import static kerbefake.Logger.info;
import static kerbefake.tests.TestUtils.*;

/**
 * A test class for client registration request - {@link RegisterClientRequest}
 */
public final class ClientRegisterTest {

    public static void main(String[] args) throws IOException {
        info("Starting test for client registration request.");
        Thread handle = startAuthServer();

        info("Preparing data for request");
        String randomId = generateRandomID();
        String name = "Ron Person\0";
        String password = "strongPassword123!\0";
        int payloadSize = name.length() + password.length();

        AuthServerMessage message = new RegisterClientRequest(new AuthServerMessageHeader(randomId, (byte) 4, MessageCode.REGISTER_CLIENT, payloadSize), new RegisterClientRequestBody(name, password));

        Socket socket = new Socket("127.0.0.1", 1256);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.write(message.toLEByteArray());

        try {
            AuthServerMessage response = AuthServerMessage.parse(socket.getInputStream(), true);

            RegisterClientResponse registerRes = (RegisterClientResponse) response;
            System.out.println("Register id: " + registerRes.getBody().toString());

        } catch (InvalidMessageException e) {
            e.printStackTrace();
            error("Failed to parse response due to: %s", e);
        }

        socket.close();
        handle.interrupt();


    }


}
