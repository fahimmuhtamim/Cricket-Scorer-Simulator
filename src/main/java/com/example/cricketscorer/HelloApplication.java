package com.example.cricketscorer;

import com.example.cricketscorer.sync.SyncManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private SyncManager syncManager;

    @Override
    public void start(Stage stage) throws IOException {
        syncManager = new SyncManager("./"); // Current directory where .txt files are stored
        boolean connected = syncManager.initialize();

        if (connected) {
            System.out.println("File synchronization enabled");
        } else {
            System.out.println("Running in offline mode");
        }

        Parent root = FXMLLoader.load(getClass().getResource("log-in.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Cricket Scorer Simulator" + (connected ? " (Online)" : " (Offline)"));
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> {
            if (syncManager != null) {
                syncManager.shutdown();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
