package com.example.cricketscorer.sync;

import java.io.*;
import java.net.Socket;

class ClientHandler implements Runnable {
    private final Socket socket;
    private final FileSyncServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientId;

    public ClientHandler(Socket socket, FileSyncServer server) {
        this.socket = socket;
        this.server = server;
        this.clientId = socket.getInetAddress().toString() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Send welcome message
            FileMessage welcome = new FileMessage(FileMessage.MessageType.CLIENT_CONNECTED, "");
            sendMessage(welcome);

            // Listen for messages from client
            while (!socket.isClosed()) {
                try {
                    FileMessage message = (FileMessage) in.readObject();
                    handleClientMessage(message);
                } catch (ClassNotFoundException e) {
                    System.err.println("Invalid message from client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientId);
        } finally {
            cleanup();
        }
    }

    private void handleClientMessage(FileMessage message) {
        switch (message.getType()) {
            case REQUEST_FILE:
                sendFileContent(message.getFilename());
                break;
            case FILE_UPDATED:
            case FILE_CREATED:
                // Client updated a file, broadcast to other clients
                server.broadcastToClients(message);
                break;
            case HEARTBEAT:
                // Respond to heartbeat
                sendMessage(new FileMessage(FileMessage.MessageType.HEARTBEAT, ""));
                break;
        }
    }

    private void sendFileContent(String filename) {
        try {
            String content = server.readFileContent(filename);
            FileMessage response = new FileMessage(FileMessage.MessageType.FILE_CONTENT, filename, content);
            sendMessage(response);
        } catch (IOException e) {
            System.err.println("Error sending file content: " + e.getMessage());
        }
    }

    public boolean sendMessage(FileMessage message) {
        try {
            out.writeObject(message);
            out.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to send message to client " + clientId + ": " + e.getMessage());
            return false;
        }
    }

    private void cleanup() {
        server.removeClient(this);
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error cleaning up client connection: " + e.getMessage());
        }
    }
}