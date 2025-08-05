package com.example.cricketscorer.sync;

import java.io.IOException;
import java.nio.file.*;

public class SyncManager {
    private FileSyncClient client;
    private WatchService localWatchService;
    private final String localDirectory;
    private volatile boolean monitoring = false;

    public SyncManager(String localDirectory) {
        this.localDirectory = localDirectory;
        this.client = new FileSyncClient(localDirectory);
    }

    public boolean initialize() {
        // Connect to sync server
        if (!client.connect()) {
            System.err.println("Failed to connect to sync server. Running in offline mode.");
            return false;
        }

        // Start monitoring local file changes
        startLocalFileMonitoring();

        return true;
    }

    private void startLocalFileMonitoring() {
        Thread monitorThread = new Thread(() -> {
            try {
                localWatchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get(localDirectory);

                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }

                path.register(localWatchService,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE);

                monitoring = true;

                while (monitoring) {
                    WatchKey key = localWatchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        String filename = event.context().toString();

                        // Only sync cricket scorer files
                        if (isCricketScorerFile(filename)) {
                            handleLocalFileChange(filename);
                        }
                    }

                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Local file monitoring error: " + e.getMessage());
            }
        });

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private boolean isCricketScorerFile(String filename) {
        return filename.endsWith(".txt") &&
                (filename.equals("tournaments.txt") ||
                        filename.equals("Users.txt") ||
                        filename.equals("Passwords.txt") ||
                        filename.contains("Cup.txt") ||
                        filename.matches(".*Tournament.*\\.txt"));
    }

    private void handleLocalFileChange(String filename) {
        try {
            // Add small delay to ensure file write is complete
            Thread.sleep(100);

            Path filePath = Paths.get(localDirectory, filename);
            if (Files.exists(filePath)) {
                String content = new String(Files.readAllBytes(filePath));
                client.notifyFileChange(filename, content);
                System.out.println("Notified server of local file change: " + filename);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error handling local file change: " + e.getMessage());
        }
    }

    // Method to manually sync a specific file
    public void syncFile(String filename) {
        try {
            Path filePath = Paths.get(localDirectory, filename);
            if (Files.exists(filePath)) {
                String content = new String(Files.readAllBytes(filePath));
                client.notifyFileChange(filename, content);
                System.out.println("Manually synced file: " + filename);
            }
        } catch (IOException e) {
            System.err.println("Error manually syncing file: " + e.getMessage());
        }
    }

    // Method to request a file from server
    public void requestFile(String filename) {
        if (client != null) {
            client.requestFile(filename);
        }
    }

    public void shutdown() {
        monitoring = false;

        if (client != null) {
            client.disconnect();
        }

        if (localWatchService != null) {
            try {
                localWatchService.close();
            } catch (IOException e) {
                System.err.println("Error closing file watcher: " + e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    // Public getter for client access
    public FileSyncClient getClient() {
        return client;
    }

    // Method to force sync of all important files
    public void syncAllUserFiles() {
        syncFile("Users.txt");
        syncFile("Passwords.txt");
        syncFile("tournaments.txt");
    }
}