package com.example.cricketscorer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAction;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class TournamentViewController {

    @FXML
    private VBox matchListVBox;
    @FXML
    private Button backButton;
    @FXML
    private Button newMatchButton; // Add this button to the FXML
    @FXML
    private Button pointTableView; // Add this button for point table
    @FXML
    private Label tournamentTitleLabel; // Add this label to show tournament name

    private Tournament tournament;
    private String currentTournamentFile;

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
        if (tournamentTitleLabel != null) {
            tournamentTitleLabel.setText(tournament.getName());
        }
        if (currentTournamentFile == null || currentTournamentFile.trim().isEmpty()) {
            String filename = tournament.getName().replaceAll("\\s+", "") + ".txt";
            this.currentTournamentFile = filename;
        }
        showMatches();
        if (pointTableView != null) {
            pointTableView.setOnAction(e -> handlePointTableView(e));
        }
    }

    public void setTournamentFile(String filename) {
        this.currentTournamentFile = filename;
        if (tournament != null) {
            showMatches();
        }
    }

    private void showMatches() {
        matchListVBox.getChildren().clear();
        if (currentTournamentFile == null || currentTournamentFile.trim().isEmpty()) {
            if (tournament != null) {
                currentTournamentFile = tournament.getName().replaceAll("\\s+", "") + ".txt";
            } else {
                System.err.println("Error: Cannot display matches without tournament file information");
                return;
            }
        }
        for (Match match : tournament.getMatches()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("match-card.fxml"));
                Node card = loader.load();
                MatchCardController controller = loader.getController();
                controller.setMatch(match, currentTournamentFile);
                matchListVBox.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading match card for: " + match.toString());
            }
        }
    }

    @FXML
    public void handleNewMatch(ActionEvent event) {
        if (LogIn.viewer) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Viewer Restriction");
            alert.setContentText("You cannot start a new match as a viewer.");
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("new-match.fxml"));
            Parent root = loader.load();
            NewMatchController controller = loader.getController();
            if (currentTournamentFile == null || currentTournamentFile.trim().isEmpty()) {
                if (tournament != null) {
                    currentTournamentFile = tournament.getName().replaceAll("\\s+", "") + ".txt";
                } else {
                    System.err.println("Error: Cannot create new match without tournament information");
                    return;
                }
            }
            controller.setTournamentFile(currentTournamentFile);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("New Match");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePointTableView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("point-table.fxml"));
            Parent root = loader.load();
            PointTableController controller = loader.getController();
            controller.setTournament(tournament);
            if (currentTournamentFile == null || currentTournamentFile.trim().isEmpty()) {
                if (tournament != null) {
                    currentTournamentFile = tournament.getName().replaceAll("\\s+", "") + ".txt";
                }
            }
            controller.setTournamentFile(currentTournamentFile);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Point Table");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goBack(ActionEvent e) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("tournament-list.fxml"));
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public Tournament getTournament() {
        return tournament;
    }
    public String getCurrentTournamentFile() {
        return currentTournamentFile;
    }
}