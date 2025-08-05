package com.example.cricketscorer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MatchCardController {

    @FXML private Label matchLabel;
    @FXML private Button viewButton;
    private Match match;
    private String tournamentFile;
    public void setMatch(Match match, String tournamentFile) {
        if (tournamentFile == null || tournamentFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Tournament file path cannot be null or empty");
        }
        this.match = match;
        this.tournamentFile = tournamentFile;
        matchLabel.setText(match.toString());
        viewButton.setOnAction(e -> openMatchView());
    }

    private boolean hasActualMatchData() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(tournamentFile));
            boolean foundMatch = false;
            int lineIndex = 0;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("|") && line.contains(match.getTeamAName()) &&
                        line.contains(match.getTeamBName())) {
                    foundMatch = true;
                    lineIndex = i;
                    break;
                }
            }
            if (foundMatch && lineIndex + 7 < lines.size()) {
                String teamABatting = lines.get(lineIndex + 4);
                String teamBBowling = lines.get(lineIndex + 5);
                String teamBBatting = lines.get(lineIndex + 6);
                String teamABowling = lines.get(lineIndex + 7);
                boolean hasFirstInningsData = hasValidBowlingData(teamBBowling);
                boolean hasSecondInningsData = hasValidBowlingData(teamABowling);
                boolean hasFirstInningsBatting = teamABatting != null && !teamABatting.trim().isEmpty();
                boolean hasSecondInningsBatting = teamBBatting != null && !teamBBatting.trim().isEmpty();
                return (hasFirstInningsBatting && hasFirstInningsData) ||
                        (hasSecondInningsBatting && hasSecondInningsData);
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasValidBowlingData(String bowlingData) {
        if (bowlingData == null || bowlingData.trim().isEmpty()) {
            return false;
        }
        String[] bowlerEntries = bowlingData.trim().split("\\s+");
        for (String entry : bowlerEntries) {
            if (entry.contains("|")) {
                String[] parts = entry.split("\\|", 2);
                if (parts.length == 2) {
                    String ballSequence = parts[1].trim();
                    if (!ballSequence.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void openMatchView() {
        try {
            String fxmlFile;
            String windowTitle;
            if (match.finished() || LogIn.viewer) {
                fxmlFile = "match-view.fxml";
                windowTitle = "Match Details";
            } else if (hasActualMatchData()) {
                fxmlFile = "current-match.fxml";
                windowTitle = "Live Match Scoring";
            } else {
                fxmlFile = "current-match.fxml";
                windowTitle = "New Match Setup";
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            if (fxmlFile.equals("match-view.fxml")) {
                MatchViewController controller = loader.getController();
                controller.setMatch(match);
            } else {
                CurrentMatchController controller = loader.getController();
                controller.setMatch(match, tournamentFile);
            }
            Stage stage = new Stage();
            stage.setTitle(windowTitle);
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("match-view.fxml"));
                Scene scene = new Scene(loader.load());
                MatchViewController controller = loader.getController();
                controller.setMatch(match);
                Stage stage = new Stage();
                stage.setTitle("Match Details");
                stage.setScene(scene);
                stage.show();
            } catch (Exception fallbackEx) {
                fallbackEx.printStackTrace();
            }
        }
    }

    public Match getMatch() {
        return match;
    }

    public String getTournamentFile() {
        return tournamentFile;
    }
}