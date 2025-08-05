package com.example.cricketscorer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NewMatchController {

    @FXML private TextField team1NameField;
    @FXML private TextField team2NameField;
    @FXML private TextField oversField;

    // Team 1 player fields
    @FXML private TextField team1Player1;
    @FXML private TextField team1Player2;
    @FXML private TextField team1Player3;
    @FXML private TextField team1Player4;
    @FXML private TextField team1Player5;
    @FXML private TextField team1Player6;
    @FXML private TextField team1Player7;
    @FXML private TextField team1Player8;
    @FXML private TextField team1Player9;
    @FXML private TextField team1Player10;
    @FXML private TextField team1Player11;

    // Team 2 player fields
    @FXML private TextField team2Player1;
    @FXML private TextField team2Player2;
    @FXML private TextField team2Player3;
    @FXML private TextField team2Player4;
    @FXML private TextField team2Player5;
    @FXML private TextField team2Player6;
    @FXML private TextField team2Player7;
    @FXML private TextField team2Player8;
    @FXML private TextField team2Player9;
    @FXML private TextField team2Player10;
    @FXML private TextField team2Player11;

    @FXML private Button startMatchButton;

    private String currentTournamentFile;

    public void setTournamentFile(String tournamentFile) {
        this.currentTournamentFile = tournamentFile;
        System.out.println("Tournament file set to: " + tournamentFile); // Debug log
    }

    @FXML
    private void initialize() {
        oversField.setText("20");
    }

    @FXML
    private void handleStartMatch(ActionEvent event) {
        if (validateInputs()) {
            try {
                saveMatchToTournament();
                showSuccessAlert();
                goBackToTournament(event);
            } catch (IOException e) {
                showErrorAlert("Error saving match: " + e.getMessage());
                e.printStackTrace(); // Debug log
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            goBackToTournament(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        // Validate team names
        if (team1NameField.getText().trim().isEmpty()) {
            showErrorAlert("Please enter Team 1 name");
            return false;
        }

        if (team2NameField.getText().trim().isEmpty()) {
            showErrorAlert("Please enter Team 2 name");
            return false;
        }

        // Validate overs
        try {
            int overs = Integer.parseInt(oversField.getText().trim());
            if (overs <= 0) {
                showErrorAlert("Overs must be a positive number");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Please enter a valid number for overs");
            return false;
        }

        // Validate that at least some players are entered for each team
        List<String> team1Players = getTeam1Players();
        List<String> team2Players = getTeam2Players();

        if (team1Players.isEmpty()) {
            showErrorAlert("Please enter at least one player for Team 1");
            return false;
        }

        if (team2Players.isEmpty()) {
            showErrorAlert("Please enter at least one player for Team 2");
            return false;
        }

        return true;
    }

    private List<String> getTeam1Players() {
        List<String> players = new ArrayList<>();
        TextField[] team1Fields = {
                team1Player1, team1Player2, team1Player3, team1Player4, team1Player5,
                team1Player6, team1Player7, team1Player8, team1Player9, team1Player10, team1Player11
        };

        for (TextField field : team1Fields) {
            String player = field.getText().trim();
            if (!player.isEmpty()) {
                players.add(player);
            }
        }

        return players;
    }

    private List<String> getTeam2Players() {
        List<String> players = new ArrayList<>();
        TextField[] team2Fields = {
                team2Player1, team2Player2, team2Player3, team2Player4, team2Player5,
                team2Player6, team2Player7, team2Player8, team2Player9, team2Player10, team2Player11
        };

        for (TextField field : team2Fields) {
            String player = field.getText().trim();
            if (!player.isEmpty()) {
                players.add(player);
            }
        }

        return players;
    }

    private void saveMatchToTournament() throws IOException {
        // Check if tournament file is set
        if (currentTournamentFile == null || currentTournamentFile.trim().isEmpty()) {
            throw new IOException("No tournament file specified. Please create or select a tournament first.");
        }

        String team1Name = team1NameField.getText().trim();
        String team2Name = team2NameField.getText().trim();
        int overs = Integer.parseInt(oversField.getText().trim());

        List<String> team1Players = getTeam1Players();
        List<String> team2Players = getTeam2Players();

        // Create the full file path
        File file = new File(currentTournamentFile);
        System.out.println("Attempting to write to file: " + file.getAbsolutePath()); // Debug log

        // Check if file exists
        if (!file.exists()) {
            throw new IOException("Tournament file not found: " + currentTournamentFile);
        }

        // Read existing tournament file content
        List<String> existingLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                existingLines.add(line);
            }
        }

        // Add teams to the Teams section if they don't exist
        addTeamIfNotExists(existingLines, team1Name);
        addTeamIfNotExists(existingLines, team2Name);

        // Append the new match at the end of the file
        // Following the exact format from AsiaCup.txt
        existingLines.add(team1Name + "|" + team2Name);
        existingLines.add("Overs: " + overs);

        // Team 1 players line (with trailing |)
        StringBuilder team1Line = new StringBuilder();
        for (String player : team1Players) {
            team1Line.append(player).append("|");
        }
        existingLines.add(team1Line.toString());

        // Team 2 players line (with trailing |)
        StringBuilder team2Line = new StringBuilder();
        for (String player : team2Players) {
            team2Line.append(player).append("|");
        }
        existingLines.add(team2Line.toString());

        // Add 4 empty lines for match data (to be filled during actual match)
        existingLines.add(""); // Team 1 batting lineup (initially empty)
        existingLines.add(""); // Team 1 bowling stats (initially empty)
        existingLines.add(""); // Team 2 batting lineup (initially empty)
        existingLines.add(""); // Team 2 bowling stats (initially empty)

        // Write all content back to the file
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String line : existingLines) {
                writer.println(line);
            }
        }

        System.out.println("Match successfully added to tournament file"); // Debug log
    }

    private void addTeamIfNotExists(List<String> lines, String teamName) {
        // Find Teams: section
        int teamsIndex = -1;
        int matchesIndex = -1;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).equals("Teams:")) {
                teamsIndex = i;
            } else if (lines.get(i).equals("Matches:")) {
                matchesIndex = i;
                break;
            }
        }

        // If Teams: section doesn't exist, create it
        if (teamsIndex == -1) {
            if (matchesIndex != -1) {
                lines.add(matchesIndex, "Teams:");
                teamsIndex = matchesIndex;
                matchesIndex++;
            } else {
                // Add Teams: section before creating Matches: section
                lines.add("Teams:");
                lines.add("Matches:");
                teamsIndex = lines.size() - 2;
                matchesIndex = lines.size() - 1;
            }
        }

        // If Matches: section doesn't exist, create it
        if (matchesIndex == -1) {
            lines.add("Matches:");
            matchesIndex = lines.size() - 1;
        }

        // Check if team already exists in Teams section
        boolean teamExists = false;
        for (int i = teamsIndex + 1; i < matchesIndex; i++) {
            if (lines.get(i).trim().equals(teamName)) {
                teamExists = true;
                break;
            }
        }

        // Add team if it doesn't exist
        if (!teamExists) {
            lines.add(matchesIndex, teamName);
        }
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Match created successfully!");
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goBackToTournament(ActionEvent event) throws IOException {
        // Go back to tournament view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("tournament-view.fxml"));
        Parent root = loader.load();

        // Load the tournament and pass it to the controller
        if (currentTournamentFile != null) {
            Tournament tournament = Tournament.loadFromFile(currentTournamentFile);
            TournamentViewController controller = loader.getController();
            controller.setTournament(tournament);
            controller.setTournamentFile(currentTournamentFile); // Also set the file name
        }

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Tournament View");
        stage.setScene(scene);
        stage.show();
    }
}