package com.example.cricketscorer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class PointTableController {

    @FXML private Label tournamentNameLabel;
    @FXML private TableView<TeamStats> pointTable;
    @FXML private TableColumn<TeamStats, String> teamColumn;
    @FXML private TableColumn<TeamStats, Integer> matchesPlayedColumn;
    @FXML private TableColumn<TeamStats, Integer> winsColumn;
    @FXML private TableColumn<TeamStats, Integer> lossesColumn;
    @FXML private TableColumn<TeamStats, Integer> pointsColumn;
    @FXML private TableColumn<TeamStats, String> netRunRateColumn;
    @FXML private Button backButton;

    private Tournament tournament;
    private String currentTournamentFile;

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
        if (tournament != null) {
            tournamentNameLabel.setText(tournament.getName() + " - Point Table");
            calculateAndDisplayPointTable();
        }
    }

    public void setTournamentFile(String tournamentFile) {
        this.currentTournamentFile = tournamentFile;
    }

    @FXML
    private void initialize() {
        // Set up table columns
        teamColumn.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        matchesPlayedColumn.setCellValueFactory(new PropertyValueFactory<>("matchesPlayed"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        lossesColumn.setCellValueFactory(new PropertyValueFactory<>("losses"));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
        netRunRateColumn.setCellValueFactory(new PropertyValueFactory<>("netRunRateString"));
    }

    private void calculateAndDisplayPointTable() {
        Map<String, TeamStats> teamStatsMap = new HashMap<>();

        // Initialize team stats for all teams
        for (String teamName : tournament.getTeamNames()) {
            teamStatsMap.put(teamName, new TeamStats(teamName));
        }

        // Process each match
        for (Match match : tournament.getMatches()) {
            String teamA = match.getTeamAName();
            String teamB = match.getTeamBName();

            TeamStats statsA = teamStatsMap.get(teamA);
            TeamStats statsB = teamStatsMap.get(teamB);

            if (statsA == null) {
                statsA = new TeamStats(teamA);
                teamStatsMap.put(teamA, statsA);
            }
            if (statsB == null) {
                statsB = new TeamStats(teamB);
                teamStatsMap.put(teamB, statsB);
            }

            Match.TeamScore scoreA = match.getTeamAScore();
            Match.TeamScore scoreB = match.getTeamBScore();

            double oversA = calculateOversCompleted(match.getBowlingB());
            double oversB = calculateOversCompleted(match.getBowlingA());

            statsA.matchesPlayed++;
            statsB.matchesPlayed++;

            statsA.totalRuns += scoreA.runs;
            statsA.totalOvers += oversA;
            statsA.totalRunsAgainst += scoreB.runs;
            statsA.totalOversAgainst += oversB;

            statsB.totalRuns += scoreB.runs;
            statsB.totalOvers += oversB;
            statsB.totalRunsAgainst += scoreA.runs;
            statsB.totalOversAgainst += oversA;

            if (match.finished()) {
                String result = match.getMatchResult();
                if (result.contains(teamA + " won")) {
                    statsA.wins++;
                    statsB.losses++;
                } else if (result.contains(teamB + " won")) {
                    statsB.wins++;
                    statsA.losses++;
                }
                // If tied, no wins or losses are added
            }
        }

        // Calculate net run rates and points
        for (TeamStats stats : teamStatsMap.values()) {
            stats.calculateNetRunRate();
            stats.points = stats.wins * 2;
        }

        // Sort teams by points (descending), then by net run rate (descending)
        List<TeamStats> sortedStats = new ArrayList<>(teamStatsMap.values());
        sortedStats.sort((a, b) -> {
            // First compare by points (higher points first)
            if (a.points != b.points) {
                return Integer.compare(b.points, a.points);
            }
            // If points are same, compare by net run rate (higher NRR first)
            return Double.compare(b.netRunRate, a.netRunRate);
        });

        // Display in table
        ObservableList<TeamStats> data = FXCollections.observableArrayList(sortedStats);
        pointTable.setItems(data);
    }

    private double calculateOversCompleted(List<String> bowlingData) {
        int totalBalls = 0;

        for (String bowlerStats : bowlingData) {
            if (bowlerStats != null && bowlerStats.contains(":")) {
                String[] parts = bowlerStats.split(":", 2);
                if (parts.length >= 2) {
                    String ballSequence = parts[1].trim();

                    if (!ballSequence.isEmpty()) {
                        CricketDataParser.BowlingFigures figures =
                                CricketDataParser.calculateBowlingFigures(ballSequence);
                        totalBalls += figures.balls;
                    }
                }
            }
        }

        // Convert total balls to overs (6 balls = 1 over)
        return totalBalls / 6.0;
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tournament-view.fxml"));
            Parent root = loader.load();

            TournamentViewController controller = loader.getController();
            controller.setTournament(tournament);
            controller.setTournamentFile(currentTournamentFile);

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to represent team statistics
    public static class TeamStats {
        private String teamName;
        private int matchesPlayed;
        private int wins;
        private int losses;
        private int points;
        private double totalRuns;
        private double totalOvers;
        private double totalRunsAgainst;
        private double totalOversAgainst;
        private double netRunRate;

        public TeamStats(String teamName) {
            this.teamName = teamName;
            this.matchesPlayed = 0;
            this.wins = 0;
            this.losses = 0;
            this.points = 0;
            this.totalRuns = 0;
            this.totalOvers = 0;
            this.totalRunsAgainst = 0;
            this.totalOversAgainst = 0;
            this.netRunRate = 0.0;
        }

        public void calculateNetRunRate() {
            double runRateFor = (totalOvers > 0) ? totalRuns / totalOvers : 0;
            double runRateAgainst = (totalOversAgainst > 0) ? totalRunsAgainst / totalOversAgainst : 0;
            this.netRunRate = runRateFor - runRateAgainst;
        }

        // Getters for TableView
        public String getTeamName() { return teamName; }
        public int getMatchesPlayed() { return matchesPlayed; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getPoints() { return points; }
        public double getNetRunRate() { return netRunRate; }

        public String getNetRunRateString() {
            return String.format("%.3f", netRunRate);
        }
    }
}