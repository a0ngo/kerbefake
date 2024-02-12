package kerbefake;

import kerbefake.errors.InvalidRequestException;
import kerbefake.models.auth_server.requests.AuthServerRequest;
import kerbefake.models.auth_server.requests.AuthServerRequestBody;
import kerbefake.models.auth_server.AuthServerRequestHeader;
import kerbefake.models.auth_server.RequestCode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import static kerbefake.Logger.error;

/**
 *
 */
public class AuthServerConnectionHandler implements Runnable {

    private Socket conn;

    public AuthServerConnectionHandler(Socket conn) {
        assert conn != null;
        this.conn = conn;
    }

    @Override
    public void run() {
        BufferedInputStream in;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(conn.getInputStream());
            out = new BufferedOutputStream(conn.getOutputStream());
        } catch (IOException e) {
            error("Failed to created stream reader and writer: %s", e);
            return;
        }

        while (true) {
            AuthServerRequestHeader header = this.readHeader(in);
            AuthServerRequestBody body = null;
            if(header == null){
                continue;
            }
            if(header.getPayloadSize() != 0){
                body = this.readBody(in, header.getPayloadSize(), header.getCode());
                if(body == null){
                    continue;
                }
            }

            try {
                AuthServerRequest.buildFor(header, body);
            } catch (InvalidRequestException e) {
                error("Failed to build request: %s", e);
                continue;
            }

        }
    }


    /**
     * Reads the header from the input stream.
     * @param in - the input strema
     * @return An {@link AuthServerRequestHeader} or null in case of an error.
     */
    private AuthServerRequestHeader readHeader(BufferedInputStream in) {
        try {
            byte[] headerBytes = new byte[Constants.REQUEST_HEADER_SIZE];
            int readBytes = in.read(headerBytes);

            /*
             * -1 is required here because there might be a request with only 23 bytes (just the header, with 0 payload)
             */
            if (readBytes != Constants.REQUEST_HEADER_SIZE && readBytes != -1) {
                error("Failed to read header, expected 23 bytes but got %d", readBytes);
                return null;
            }

            return AuthServerRequestHeader.parseHeader(headerBytes);
        } catch (IOException e) {
            error("Failed to read request header from input stream due to: %s", e);
            return null;
        } catch (InvalidRequestException e) {
            error("%s", e);
            return null;
        }

    }

    private AuthServerRequestBody readBody(BufferedInputStream in, int size, RequestCode code){
        byte[] bodyBytes = new byte[size];

        try{
            int readBytes = in.read(bodyBytes);
            if(readBytes != size && readBytes != -1){
                error("Failed to read body, expected %d bytes, but got %d", readBytes);
                return null;
            }

            return AuthServerRequestBody.parse(bodyBytes, code);
        } catch (IOException e) {
            error("Failed to read request body from input stream due to: %s", e);
            return null;
        } catch (InvalidRequestException e) {
            error("%s", e);
            return null;
        }

    }
}
