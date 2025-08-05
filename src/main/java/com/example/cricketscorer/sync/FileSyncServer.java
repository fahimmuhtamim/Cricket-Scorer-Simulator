package com.example.cricketscorer.sync;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class FileSyncServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private WatchService watchService;
    private final String syncDirectory;

    public FileSyncServer(String syncDirectory) {
        this.syncDirectory = syncDirectory;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Cricket Scorer Sync Server started on port " + PORT);
        System.out.println("Monitoring directory: " + syncDirectory);

        // Start file watcher
        startFileWatcher();

        // Accept client connections
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                executor.submit(client);
                System.out.println("Client connected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    private void startFileWatcher() {
        executor.submit(() -> {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get(syncDirectory);

                // Create directory if it doesn't exist
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                }

                path.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);

                while (running) {
                    WatchKey key = watchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        String filename = event.context().toString();

                        // Only sync specific cricket scorer files
                        if (isCricketScorerFile(filename)) {
                            handleFileChange(kind, filename);
                        }
                    }

                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("File watcher error: " + e.getMessage());
            }
        });
    }

    private boolean isCricketScorerFile(String filename) {
        return filename.endsWith(".txt") &&
                (filename.equals("tournaments.txt") ||
                        filename.equals("Users.txt") ||
                        filename.equals("Passwords.txt") ||
                        filename.contains("Cup.txt") || // Tournament files
                        filename.matches(".*Tournament.*\\.txt")); // Other tournament files
    }

    private void handleFileChange(WatchEvent.Kind<?> kind, String filename) {
        try {
            FileMessage.MessageType messageType;
            String content = null;

            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                messageType = FileMessage.MessageType.FILE_CREATED;
                content = readFileContent(filename);
            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                messageType = FileMessage.MessageType.FILE_UPDATED;
                content = readFileContent(filename);
            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                messageType = FileMessage.MessageType.FILE_DELETED;
            } else {
                return;
            }

            FileMessage message = new FileMessage(messageType, filename, content);
            broadcastToClients(message);

            System.out.println("File change detected: " + kind + " - " + filename);

        } catch (IOException e) {
            System.err.println("Error handling file change: " + e.getMessage());
        }
    }

    public String readFileContent(String filename) throws IOException {
        Path filePath = Paths.get(syncDirectory, filename);
        if (Files.exists(filePath)) {
            return new String(Files.readAllBytes(filePath));
        }
        return null;
    }

    public void broadcastToClients(FileMessage message) {
        Iterator<ClientHandler> iterator = clients.iterator();
        while (iterator.hasNext()) {
            ClientHandler client = iterator.next();
            if (!client.sendMessage(message)) {
                iterator.remove(); // Remove disconnected clients
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public void stop() {
        running = false;
        executor.shutdown();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String syncDir = args.length > 0 ? args[0] : "./cricket_data";
        FileSyncServer server = new FileSyncServer(syncDir);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
