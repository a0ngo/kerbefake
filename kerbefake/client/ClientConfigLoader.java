package kerbefake.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ClientConfigLoader {
    private static final String CONFIG_FILE_NAME = "client.config";
    private Properties properties;

    public ClientConfigLoader() {
        properties = new Properties();
        loadConfiguration();
    }

    private void loadConfiguration() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Could not load the configuration file. Make sure " + CONFIG_FILE_NAME + " exists.");
            e.printStackTrace();
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
