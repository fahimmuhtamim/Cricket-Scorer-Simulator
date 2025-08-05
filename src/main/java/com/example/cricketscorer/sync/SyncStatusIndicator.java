package com.example.cricketscorer.sync;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class SyncStatusIndicator {
    private Label statusLabel;
    private SyncManager syncManager;

    public SyncStatusIndicator(Label statusLabel, SyncManager syncManager) {
        this.statusLabel = statusLabel;
        this.syncManager = syncManager;
        updateStatus();

        // Update status periodically
        Thread statusUpdater = new Thread(() -> {
            while (true) {
                Platform.runLater(this::updateStatus);
                try {
                    Thread.sleep(5000); // Update every 5 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        statusUpdater.setDaemon(true);
        statusUpdater.start();
    }

    private void updateStatus() {
        if (syncManager.isConnected()) {
            statusLabel.setText("● Online");
            statusLabel.setTextFill(Color.GREEN);
        } else {
            statusLabel.setText("● Offline");
            statusLabel.setTextFill(Color.RED);
        }
    }
}