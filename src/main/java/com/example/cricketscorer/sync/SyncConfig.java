package com.example.cricketscorer.sync;

import java.util.Properties;
import java.io.*;

public class SyncConfig {
    private static final String CONFIG_FILE = "sync.properties";
    private Properties properties;

    public SyncConfig() {
        properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            // Create default config
            setDefaults();
            saveConfig();
        }
    }

    private void setDefaults() {
        // CHANGE THIS IP TO YOUR SERVER'S ACTUAL IP ADDRESS
        properties.setProperty("server.host", "10.18.37.99");
        properties.setProperty("server.port", "8888");
        properties.setProperty("sync.enabled", "true");
        properties.setProperty("heartbeat.interval", "30");
        properties.setProperty("reconnect.attempts", "5");
        properties.setProperty("reconnect.delay", "5000");
    }

    public void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Cricket Scorer Sync Configuration");
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    public String getServerHost() {
        return properties.getProperty("server.host", "10.18.37.99"); // Change default here too
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "8888"));
    }

    public boolean isSyncEnabled() {
        return Boolean.parseBoolean(properties.getProperty("sync.enabled", "true"));
    }

    public int getHeartbeatInterval() {
        return Integer.parseInt(properties.getProperty("heartbeat.interval", "30"));
    }

    public int getReconnectAttempts() {
        return Integer.parseInt(properties.getProperty("reconnect.attempts", "5"));
    }

    public long getReconnectDelay() {
        return Long.parseLong(properties.getProperty("reconnect.delay", "5000"));
    }

    public void setServerHost(String host) {
        properties.setProperty("server.host", host);
    }

    public void setServerPort(int port) {
        properties.setProperty("server.port", String.valueOf(port));
    }

    public void setSyncEnabled(boolean enabled) {
        properties.setProperty("sync.enabled", String.valueOf(enabled));
    }
}