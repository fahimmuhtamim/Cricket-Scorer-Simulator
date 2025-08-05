package com.example.cricketscorer.sync;

import java.io.Serializable;

public class FileMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        FILE_UPDATED,
        FILE_CREATED,
        FILE_DELETED,
        REQUEST_FILE,
        FILE_CONTENT,
        CLIENT_CONNECTED,
        HEARTBEAT
    }

    private MessageType type;
    private String filename;
    private String content;
    private long timestamp;
    private String clientId;

    public FileMessage(MessageType type, String filename) {
        this.type = type;
        this.filename = filename;
        this.timestamp = System.currentTimeMillis();
    }

    public FileMessage(MessageType type, String filename, String content) {
        this(type, filename);
        this.content = content;
    }

    // Getters and setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
}
