package kerbefake;

import kerbefake.errors.InvalidRequestException;
import kerbefake.models.auth_server.AuthServerRequestHeader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        }
    }


    private AuthServerRequestHeader readHeader(BufferedInputStream in) {
        try {
            byte[] headerBytes = new byte[Constants.REQUEST_HEADER_SIZE];
            int readBytes = in.read(headerBytes);

            /*
             * -1 is required here because there might be a request with only 23 bytes (just the header, with 0 payload)
             */
            if (readBytes != Constants.REQUEST_HEADER_SIZE || readBytes != -1) {
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
}
