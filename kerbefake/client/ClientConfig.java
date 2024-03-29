package kerbefake.client;

import kerbefake.client.errors.InvalidClientConfigException;
import kerbefake.common.Constants;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static kerbefake.common.Constants.CLIENT_CONFIG_FILE_NAME;
import static kerbefake.common.Constants.ID_LENGTH;
import static kerbefake.common.Logger.error;
import static kerbefake.common.Logger.warn;
import static kerbefake.common.Utils.performSha256OnValue;

public final class ClientConfig {

    private String name;

    private String clientIdHex;

    /**
     * The password hash used to decrypt message from the auth server.
     */
    private byte[] passwordHash;

    /**
     * The plaintext password, stored as a char array so we can simply zero its content when we uesd it.
     */
    private char[] password;

    private ClientConfig() {
    }

    private ClientConfig(String name, String clientIdHex) {
        this.name = name;
        this.clientIdHex = clientIdHex;
    }

    public String getName() {
        return name;
    }

    public String getClientIdHex() {
        return clientIdHex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClientIdHex(String clientIdHex) {
        this.clientIdHex = clientIdHex;
    }

    public void hashAndSetPassword(char[] password) {
        try {
            this.passwordHash = performSha256OnValue(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            error("This machine does not support SHA-256, can't proceed, exiting.");
            System.exit(1);
        }
    }

    public byte[] getHashedPassword() {
        return this.passwordHash;
    }

    public static ClientConfig createEmpty() {
        return new ClientConfig();
    }

    /**
     * Loads the client configuration from {@link Constants#CLIENT_CONFIG_FILE_NAME} (./me.config).
     *
     * @return a {@link ClientConfig} object
     */
    public static ClientConfig load() throws InvalidClientConfigException {
        try {
            BufferedReader input = new BufferedReader(new FileReader(CLIENT_CONFIG_FILE_NAME));
            String name = input.readLine();
            String clientId = input.readLine();
            try {
                String additionalRead = input.readLine();
                if (additionalRead != null && !additionalRead.isEmpty()) {
                    warn("Client configuration has more than 2 lines, trying to use the first two lines. Please remove empty lines from file.");
                }
            } catch (IOException e) {
                // Ignoring since we expect only two lines
            }

            if (name == null || name.isEmpty()) {
                throw new InvalidClientConfigException("Missing user name, it cannot be empty");
            }

            if (clientId == null || clientId.length() != ID_LENGTH) {
                throw new InvalidClientConfigException("Missing user client ID, must be 32 hex characters");
            }

            return new ClientConfig(name, clientId);
        } catch (FileNotFoundException e) {
            throw new InvalidClientConfigException();
        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidClientConfigException(String.format("Failed to read client configuration due to: %s", e.getMessage()));
        }
    }

    public void setPlainTextPassword(char[] password) {
        this.password = password;
    }


    public char[] getPlainTextPassword() {
        return this.password;
    }

    public void clearPassword() {
        Arrays.fill(password, (char) 0);
        password = null;
    }


}
