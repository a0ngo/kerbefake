package kerbefake.tests;

import kerbefake.models.auth_server.AuthServerRequestHeader;
import kerbefake.models.auth_server.RequestCode;
import kerbefake.models.auth_server.requests.RegisterClientRequestBody;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import static kerbefake.Logger.info;
import static kerbefake.tests.TestUtils.generateRandomID;
import static kerbefake.tests.TestUtils.startAuthServer;

/**
 * A test class for client registration request - {@link kerbefake.models.auth_server.requests.RegisterClientRequest}
 */
public final class ClientRegisterTest {

    public static void main(String[] args) throws IOException {
        info("Starting test for client registration request.");
        startAuthServer();

        info("Preparing data for request");
//        AuthServerRequestHeader header = new AuthServerRequestHeader(generateRandomID(), (byte) 4, RequestCode.REGISTER_CLIENT, name.length() + password.length() + 2);
        byte[] requestHeader = new byte[19];
        String randomId = generateRandomID();
        String name = "Ron Person";
        String password = "strongPassword123!";

        for(int i = 0; i < 16 ; i++){
            requestHeader[i] = (byte)randomId.charAt(i);
        }

        requestHeader[16] = 4;

        byte[] requestBytes = new byte[requestHeader.length + name.length() + password.length() + 2];
        int reqByteIdx = 0;
       // byte[] headerBytes = header.getRawHeader();
        // for (int i = 0; i < headerBytes.length; i++) {
        //     requestBytes[reqByteIdx++] = headerBytes[i];
        //  }

        for (int i = 0; i < requestHeader.length; i++) {
            requestBytes[reqByteIdx++] = requestHeader[i];
        }

        byte[] nameBytes = name.getBytes();
        byte[] passBytes = password.getBytes();
        for (int i = 0; i <= nameBytes.length; i++) {
            requestBytes[reqByteIdx++] = i == nameBytes.length - 1 ? (byte) 0 : nameBytes[i];
        }
        for (int i = 0; i <= passBytes.length; i++) {
            requestBytes[reqByteIdx++] =  i == passBytes.length - 1 ? (byte) 0 : passBytes[i];
        }

        Socket socket = new Socket("127.0.0.1", 1256);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.write(requestBytes);

        System.out.println(in.readLine());

    }


}
