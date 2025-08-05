package com.example.cricketscorer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;

public class TournamentListController {
    @FXML private VBox tournamentListVBox;
    @FXML private Button backButton, newTournamentButton;
    @FXML private Label viewerMsg;

    private final String INDEX_FILE = "tournaments.txt";

    @FXML
    public void initialize() {
        try (BufferedReader br = new BufferedReader(new FileReader(INDEX_FILE))) {
            String filename;
            while ((filename = br.readLine()) != null) {
                Tournament t = Tournament.loadFromFile(filename);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("tournament-card.fxml"));
                Node cardNode = loader.load();
                TournamentCardController controller = loader.getController();
                controller.setTournament(t);
                controller.setTournamentFileName(filename); // Pass the actual filename
                tournamentListVBox.getChildren().add(cardNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        backButton.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("log-in.fxml"));
                Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public void startNewTournament(javafx.event.ActionEvent e) throws IOException {
        // Check if user is a viewer
        if (LogIn.viewer) {
            // Show error alert for viewers
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Viewer Restriction");
            alert.setContentText("You cannot create a new tournament as a viewer.");
            alert.showAndWait();
            return;
        }

        Parent root = FXMLLoader.load(getClass().getResource("new-tournament.fxml"));
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}