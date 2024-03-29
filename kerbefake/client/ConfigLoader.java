package kerbefake.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static kerbefake.Constants.CLIENT_CONFIG_FILE_NAME;

public class ConfigLoader {
    private Properties properties;

    public ConfigLoader() {
        properties = new Properties();
        loadConfiguration();
    }

    private void loadConfiguration() {
        try (FileInputStream fis = new FileInputStream(CLIENT_CONFIG_FILE_NAME)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load the configuration file. Make sure " + CLIENT_CONFIG_FILE_NAME + " exists.");
            System.exit(1);
        }
    }

    public String getServerAddress() {
        return properties.getProperty("server.address", "localhost");
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "1256"));
    }

    public boolean isCredentialsSaved() {
        return properties.containsKey("client.username") && properties.containsKey("client.password");
    }

    public String getClientUsername() {
        return properties.getProperty("client.username");
    }

    public String getClientPassword() {
        return properties.getProperty("client.password");
    }
}
