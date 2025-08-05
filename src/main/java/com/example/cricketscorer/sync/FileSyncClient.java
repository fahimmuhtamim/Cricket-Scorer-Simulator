package com.example.cricketscorer.sync;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileSyncClient {
    private static final String SERVER_HOST = "10.18.37.99"; // Change to server IP
    private static final int SERVER_PORT = 8888;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final String localDirectory;
    private volatile boolean connected = false;
    private ScheduledExecutorService executor;

    public FileSyncClient(String localDirectory) {
        this.localDirectory = localDirectory;
        this.executor = Executors.newScheduledThreadPool(2);
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;

            // Start message listener
            executor.submit(this::messageListener);

            // Start heartbeat
            executor.scheduleAtFixedRate(this::sendHeartbeat, 30, 30, TimeUnit.SECONDS);

            System.out.println("Connected to Cricket Scorer sync server");
            return true;

        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
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
                    // Server responded to heartbeat
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
        Files.createDirectories(filePath.getParent());

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
        if (!connected) return;

        try {
            FileMessage message = new FileMessage(FileMessage.MessageType.FILE_UPDATED, filename, content);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to notify server of file change: " + e.getMessage());
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
            System.err.println("Failed to send heartbeat: " + e.getMessage());
            connected = false;
        }
    }

    public void disconnect() {
        connected = false;
        executor.shutdown();

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}
