package kerbefake;

import kerbefake.errors.InvalidMessageException;
import kerbefake.models.auth_server.AuthServerMessage;
import kerbefake.models.auth_server.requests.AuthServerRequest;

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
        BufferedOutputStream out;
        try {
            in = new BufferedInputStream(conn.getInputStream());
            out = new BufferedOutputStream(conn.getOutputStream());
        } catch (IOException e) {
            error("Failed to created stream reader and writer: %s", e);
            return;
        }

        while (true) {
            try {
                AuthServerMessage message = AuthServerMessage.parse(in);

                // On this specific class we always know that we expect messages that are requests, if there's an issue we simply close the connection;
                // Because we do actually allow the parsing of a response message here.
                // This is why we hvae a catch for classcastexception below.
                AuthServerRequest req = (AuthServerRequest) message;
                AuthServerMessage res = req.execute();

                out.write(res.toLEByteArray());
                out.flush();


            } catch (InvalidMessageException | ClassCastException e) {
                // This is just an invalid message, or one we don't know how to handle - ignore and close connection.
                e.printStackTrace();
                error("Failed to parse message due to: %s", e);
                try {
                    this.conn.close();
                } catch (IOException ex) {
                    error("Failed to close socket due to: %s", e);
                }
                return;

            } catch (IOException e) {
                e.printStackTrace();
                error("Failed to send response due to: %s", e);
                // Hopefully this will be fixed later one.
            }
        }
    }


}
