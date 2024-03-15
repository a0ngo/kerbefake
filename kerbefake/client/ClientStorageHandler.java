package kerbefake.client;

import java.io.*;
import java.util.Properties;
import kerbefake.client.ClientLogger;

public class ClientStorageHandler {
    private static final String STORAGE_FILE = "client_storage.properties";
    private Properties properties;

    public ClientStorageHandler() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = new FileInputStream(STORAGE_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            ClientLogger.error("Unable to load storage properties.");
        }
    }

    public void saveProperty(String key, String value) {
        properties.setProperty(key, value);
        saveProperties();
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void saveProperties() {
        try (OutputStream output = new FileOutputStream(STORAGE_FILE)) {
            properties.store(output, null);
        } catch (IOException io) {
            ClientLogger.error("Unable to save storage properties.");
        }
    }
}
