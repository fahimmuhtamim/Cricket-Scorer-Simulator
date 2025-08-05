package com.example.cricketscorer.sync;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.concurrent.*;

public class ImprovedFileSyncClient {
    private final SyncConfig config;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final String localDirectory;
    private volatile boolean connected = false;
    private volatile boolean shouldReconnect = true;
    private ScheduledExecutorService executor;
    private int reconnectAttempts = 0;

    public ImprovedFileSyncClient(String localDirectory) {
        this.localDirectory = localDirectory;
        this.config = new SyncConfig();
        this.executor = Executors.newScheduledThreadPool(3);
    }

    public boolean connect() {
        if (!config.isSyncEnabled()) {
            System.out.println("Sync is disabled in configuration");
            return false;
        }

        return attemptConnection();
    }

    private boolean attemptConnection() {
        try {
            socket = new Socket(config.getServerHost(), config.getServerPort());
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            reconnectAttempts = 0;

            // Start message listener
            executor.submit(this::messageListener);

            // Start heartbeat
            executor.scheduleAtFixedRate(this::sendHeartbeat,
                    config.getHeartbeatInterval(),
                    config.getHeartbeatInterval(),
                    TimeUnit.SECONDS);

            System.out.println("Connected to Cricket Scorer sync server at " +
                    config.getServerHost() + ":" + config.getServerPort());
            return true;

        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            scheduleReconnect();
            return false;
        }
    }

    private void scheduleReconnect() {
        if (!shouldReconnect || reconnectAttempts >= config.getReconnectAttempts()) {
            System.err.println("Max reconnection attempts reached. Running in offline mode.");
            return;
        }

        reconnectAttempts++;
        System.out.println("Scheduling reconnection attempt " + reconnectAttempts +
                " in " + (config.getReconnectDelay() / 1000) + " seconds...");

        executor.schedule(() -> {
            if (shouldReconnect && !connected) {
                System.out.println("Attempting to reconnect...");
                attemptConnection();
            }
        }, config.getReconnectDelay(), TimeUnit.MILLISECONDS);
    }

    private void messageListener() {
        while (connected && !socket.isClosed()) {
            try {
                FileMessage message = (FileMessage) in.readObject();
                handleServerMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    System.err.println("Connection lost to server: " + e.getMessage());
                    connected = false;
                    closeConnection();
                    if (shouldReconnect) {
                        scheduleReconnect();
                    }
                }
                break;
            }
        }
    }

    private void handleServerMessage(FileMessage message) {
        try {
            switch (message.getType()) {
                case FILE_UPDATED:
                case FILE_CREATED:
                    updateLocalFile(message.getFilename(), message.getContent());
                    break;
                case FILE_DELETED:
                    deleteLocalFile(message.getFilename());
                    break;
                case FILE_CONTENT:
                    updateLocalFile(message.getFilename(), message.getContent());
                    break;
                case CLIENT_CONNECTED:
                    System.out.println("Successfully connected to sync server");
                    break;
                case HEARTBEAT:
                    // Server responded to heartbeat - connection is alive
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling server message: " + e.getMessage());
        }
    }

    private void updateLocalFile(String filename, String content) throws IOException {
        if (content == null) return;

        Path filePath = Paths.get(localDirectory, filename);

        // Create directory if it doesn't exist
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        // Write content to file
        Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("Updated local file: " + filename);
    }

    private void deleteLocalFile(String filename) throws IOException {
        Path filePath = Paths.get(localDirectory, filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            System.out.println("Deleted local file: " + filename);
        }
    }

    public void notifyFileChange(String filename, String content) {
        if (!connected) {
            System.out.println("Not connected - file change not synced: " + filename);
            return;
        }

        try {
            FileMessage message = new FileMessage(FileMessage.MessageType.FILE_UPDATED, filename, content);
            out.writeObject(message);
            out.flush();
            System.out.println("Notified server of file change: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to notify server of file change: " + e.getMessage());
            connected = false;
        }
    }

    public void requestFile(String filename) {
        if (!connected) return;

        try {
            FileMessage message = new FileMessage(FileMessage.MessageType.REQUEST_FILE, filename);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to request file: " + e.getMessage());
        }
    }

    private void sendHeartbeat() {
        if (!connected) return;

        try {
            FileMessage heartbeat = new FileMessage(FileMessage.MessageType.HEARTBEAT, "");
            out.writeObject(heartbeat);
            out.flush();
        } catch (IOException e) {
            System.err.println("Heartbeat failed: " + e.getMessage());
            connected = false;
        }
    }

    private void closeConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public void disconnect() {
        shouldReconnect = false;
        connected = false;
        executor.shutdown();
        closeConnection();
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public SyncConfig getConfig() {
        return config;
    }
}