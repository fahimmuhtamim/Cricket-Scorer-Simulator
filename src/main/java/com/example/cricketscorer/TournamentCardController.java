package com.example.cricketscorer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TournamentCardController {
    @FXML private Label tournamentNameLabel;
    @FXML private Label createdDateLabel;
    @FXML private Button openButton;

    private Tournament tournament;
    private String tournamentFileName;

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
        tournamentNameLabel.setText(tournament.getName());
        createdDateLabel.setText("Created: " + tournament.getCreatedDate());
        this.tournamentFileName = tournament.getName().replaceAll("\\s+", "") + ".txt";
        openButton.setOnAction(this::openTournamentView);
    }

    public void setTournamentFileName(String fileName) {
        this.tournamentFileName = fileName;
    }

    private void openTournamentView(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tournament-view.fxml"));
            Parent root = loader.load();
            TournamentViewController controller = loader.getController();
            controller.setTournament(tournament);
            controller.setTournamentFile(tournamentFileName);
            Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}