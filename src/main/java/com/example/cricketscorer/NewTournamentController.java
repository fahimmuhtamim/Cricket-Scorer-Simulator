package com.example.cricketscorer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.util.List;

public class NewTournamentController {
    @FXML private TextField tournamentNameField;
    @FXML private TextField team1Field, team2Field, team3Field, team4Field;
    @FXML private TextField team5Field, team6Field, team7Field;
    @FXML private Button backButton;

    private final String INDEX_FILE = "tournaments.txt";

    @FXML
    private void handleSubmit() {
        String name = tournamentNameField.getText().trim();
        if (name.isEmpty()) {
            System.out.println("Tournament name is required.");
            return;
        }

        Tournament tournament = new Tournament(name);

        for (TextField tf : List.of(team1Field, team2Field, team3Field, team4Field,
                team5Field, team6Field, team7Field)) {
            String teamName = tf.getText().trim();
            if (!teamName.isEmpty()) {
                tournament.createEmptyTeam(teamName);
            }
        }

        try {
            String filename = name.replaceAll("\\s+", "") + ".txt";
            tournament.saveToFile(filename);
            try (PrintWriter out = new PrintWriter(new FileWriter(INDEX_FILE, true))) {
                out.println(filename);
            }
            System.out.println("Tournament saved to " + filename);
            clearForm();
        } catch (IOException e) {
            System.err.println("Error saving tournament: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        tournamentNameField.clear();
        team1Field.clear();
        team2Field.clear();
        team3Field.clear();
        team4Field.clear();
        team5Field.clear();
        team6Field.clear();
        team7Field.clear();
    }

    public void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("tournament-list.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}