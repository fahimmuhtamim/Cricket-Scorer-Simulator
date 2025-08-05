package com.example.cricketscorer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import java.util.*;
import java.util.stream.Collectors;

public class MatchViewController {

    @FXML private Label team1Label;
    @FXML private Label team2Label;
    @FXML private Label team1TotalScore;
    @FXML private Label team2TotalScore;
    @FXML private Label team1OversLabel;
    @FXML private Label team2OversLabel;
    @FXML private Label matchResultLabel;

    @FXML private VBox team1BattingVBox;
    @FXML private VBox team2BowlingVBox;
    @FXML private VBox team2BattingVBox;
    @FXML private VBox team1BowlingVBox;

    @FXML private ScrollPane overviewScroll;
    @FXML private Label overviewLabel;

    public void setMatch(Match match) {
        team1Label.setText(match.getTeamAName());
        team2Label.setText(match.getTeamBName());
        Match.TeamScore teamAScore = match.getTeamAScore();
        Match.TeamScore teamBScore = match.getTeamBScore();
        team1TotalScore.setText(teamAScore.toString());
        team2TotalScore.setText(teamBScore.toString());
        double team1Overs = calculateOversCompleted(match.getBowlingB());
        double team2Overs = calculateOversCompleted(match.getBowlingA());
        if (team1OversLabel != null) {
            team1OversLabel.setText(formatOvers(team1Overs) + "/" + match.getOvers());
        }
        if (team2OversLabel != null) {
            team2OversLabel.setText(formatOvers(team2Overs) + "/" + match.getOvers());
        }
        String matchResult = match.getMatchResult();
        matchResultLabel.setText(matchResult);
        if (match.finished()) {
            matchResultLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2E8B57;");
            displayBest3Batters(team1BattingVBox, match.getTeamABattingScores(), match.getTeamAName());
            displayBest3Batters(team2BattingVBox, match.getTeamBBattingScores(), match.getTeamBName());
            displayBest3Bowlers(team2BowlingVBox, match.getBowlingB(), match.getTeamBName());
            displayBest3Bowlers(team1BowlingVBox, match.getBowlingA(), match.getTeamAName());
        } else {
            matchResultLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF6347;");
            matchResultLabel.setText("Match in Progress");
            displayCurrentBatters(team1BattingVBox, match.getBattingA(), match.getTeamABattingScores(), match.getBowlingB(), match.getTeamAName() + " - Current Batters");
            displayCurrentBatters(team2BattingVBox, match.getBattingB(), match.getTeamBBattingScores(), match.getBowlingA(), match.getTeamBName() + " - Current Batters");
            displayCurrentBowler(team2BowlingVBox, match.getBowlingB(), match.getTeamBName() + " - Current Bowler");
            displayCurrentBowler(team1BowlingVBox, match.getBowlingA(), match.getTeamAName() + " - Current Bowler");
        }
        overviewLabel.setText(formatOverview(match.getOverview()));
    }

    private double calculateOversCompleted(List<String> bowlingData) {
        int totalBalls = 0;
        for (String bowlerStats : bowlingData) {
            if (bowlerStats != null && bowlerStats.contains(":")) {
                String[] parts = bowlerStats.split(":", 2);
                if (parts.length >= 2) {
                    String ballSequence = parts[1].trim();
                    if (!ballSequence.isEmpty()) {
                        CricketDataParser.BowlingFigures figures = CricketDataParser.calculateBowlingFigures(ballSequence);
                        totalBalls += figures.balls;
                    }
                }
            }
        }
        return totalBalls / 6.0;
    }

    private String formatOvers(double overs) {
        int completeOvers = (int) overs;
        int remainingBalls = (int) Math.round((overs - completeOvers) * 6);
        if (remainingBalls >= 6) {
            completeOvers += remainingBalls / 6;
            remainingBalls = remainingBalls % 6;
        }
        if (remainingBalls == 0) {
            return String.valueOf(completeOvers);
        } else {
            return completeOvers + "." + remainingBalls;
        }
    }

    private void displayBest3Batters(VBox container, Map<String, Integer> battingScores, String title) {
        container.getChildren().clear();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2E8B57;");
        container.getChildren().add(titleLabel);
        if (battingScores.isEmpty()) {
            container.getChildren().add(new Label("No batting data available"));
            return;
        }
        List<Map.Entry<String, Integer>> sortedBatters = battingScores.entrySet().stream().sorted((a, b) -> {
                    int runsComparison = Integer.compare(b.getValue(), a.getValue());
                    if (runsComparison != 0) {
                        return runsComparison;
                    }
                    return a.getKey().compareTo(b.getKey());
                }).limit(3).collect(Collectors.toList());
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedBatters) {
            String displayText = rank + ". " + entry.getKey() + " - " + entry.getValue() + " runs";
            Label playerLabel = new Label(displayText);
            playerLabel.setStyle("-fx-padding: 2px 0px; -fx-font-size: 12px;");
            container.getChildren().add(playerLabel);
            rank++;
        }
    }

    private void displayBest3Bowlers(VBox container, List<String> bowlingData, String title) {
        container.getChildren().clear();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        container.getChildren().add(titleLabel);
        if (bowlingData.isEmpty()) {
            container.getChildren().add(new Label("No bowling data available"));
            return;
        }
        List<BowlerPerformance> bowlerPerformances = new ArrayList<>();
        for (String bowlerStats : bowlingData) {
            if (bowlerStats == null || !bowlerStats.contains(":")) continue;
            String[] parts = bowlerStats.split(":", 2);
            String bowlerName = parts[0].trim();
            String ballSequence = parts[1].trim();
            CricketDataParser.BowlingFigures figures = CricketDataParser.calculateBowlingFigures(ballSequence);
            bowlerPerformances.add(new BowlerPerformance(bowlerName, figures));
        }
        bowlerPerformances.sort((a, b) -> {
            if (a.figures.wickets != b.figures.wickets) {
                return Integer.compare(b.figures.wickets, a.figures.wickets);
            }
            if (a.figures.bowlerRuns != b.figures.bowlerRuns) {
                return Integer.compare(a.figures.bowlerRuns, b.figures.bowlerRuns);
            }
            return a.name.compareTo(b.name);
        });
        int rank = 1;
        for (int i = 0; i < Math.min(3, bowlerPerformances.size()); i++) {
            BowlerPerformance bp = bowlerPerformances.get(i);
            String displayText = String.format("%d. %s - %s-%d-%d (Econ: %.2f)",
                    rank, bp.name, bp.figures.getOverString(), bp.figures.bowlerRuns,
                    bp.figures.wickets, bp.figures.getEconomy());
            Label bowlerLabel = new Label(displayText);
            bowlerLabel.setStyle("-fx-padding: 2px 0px; -fx-font-size: 12px;");
            container.getChildren().add(bowlerLabel);
            rank++;
        }
    }

    private void displayCurrentBatters(VBox container, List<String> battingLineup, Map<String, Integer> battingScores, List<String> bowlingData, String title) {
        container.getChildren().clear();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF6347;");
        container.getChildren().add(titleLabel);
        if (battingLineup.isEmpty()) {
            container.getChildren().add(new Label("No batting data available"));
            return;
        }
        List<String> currentBatters = getCurrentBatters(battingLineup, bowlingData);

        if (currentBatters.isEmpty()) {
            currentBatters = battingLineup.subList(0, Math.min(2, battingLineup.size()));
        }
        int battersToShow = Math.min(2, currentBatters.size());
        for (int i = 0; i < battersToShow; i++) {
            String batter = currentBatters.get(i);
            Integer score = battingScores.getOrDefault(batter, 0);
            String displayText = "• " + batter + " - " + score + " runs*";
            Label batterLabel = new Label(displayText);
            batterLabel.setStyle("-fx-padding: 2px 0px; -fx-font-size: 12px; -fx-text-fill: #FF6347;");
            container.getChildren().add(batterLabel);
        }
        if (battersToShow > 0) {
            Label noteLabel = new Label("*Currently batting");
            noteLabel.setStyle("-fx-padding: 2px 0px; -fx-font-size: 10px; -fx-font-style: italic;");
            container.getChildren().add(noteLabel);
        }
    }

    private void displayCurrentBowler(VBox container, List<String> bowlingData, String title) {
        container.getChildren().clear();
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF6347;");
        container.getChildren().add(titleLabel);
        if (bowlingData.isEmpty()) {
            container.getChildren().add(new Label("No bowling data available"));
            return;
        }
        String currentBowlerStats = bowlingData.get(bowlingData.size() - 1);
        if (currentBowlerStats != null && currentBowlerStats.contains(":")) {
            String[] parts = currentBowlerStats.split(":", 2);
            String bowlerName = parts[0].trim();
            String ballSequence = parts[1].trim();
            CricketDataParser.BowlingFigures figures = CricketDataParser.calculateBowlingFigures(ballSequence);
            String displayText = String.format("• %s - %s-%d-%d (Econ: %.2f)*",
                    bowlerName, figures.getOverString(), figures.bowlerRuns,
                    figures.wickets, figures.getEconomy());
            Label bowlerLabel = new Label(displayText);
            bowlerLabel.setStyle("-fx-padding: 2px 0px; -fx-font-size: 12px; -fx-text-fill: #FF6347;");
            container.getChildren().add(bowlerLabel);
            Label noteLabel = new Label("*Currently bowling");
            noteLabel.setStyle("-fx-padding: 2px 0px; -fx-font-size: 10px; -fx-font-style: italic;");
            container.getChildren().add(noteLabel);
        }
    }

    private List<String> getCurrentBatters(List<String> battingLineup, List<String> bowlingData) {
        List<String> currentBatters = new ArrayList<>();
        if (battingLineup.isEmpty() || bowlingData.isEmpty()) {
            return currentBatters;
        }
        int currentBatsmanIndex = 0;
        int wicketsTaken = 0;
        for (String bowlerStats : bowlingData) {
            if (!bowlerStats.contains(":")) continue;
            String[] parts = bowlerStats.split(":", 2);
            String ballSequence = parts[1].trim();
            String[] balls = ballSequence.split("\\s+");
            for (String ball : balls) {
                ball = ball.trim();
                if (ball.isEmpty()) continue;
                if (ball.equals("wk") || ball.contains("+wk")) {
                    wicketsTaken++;
                    currentBatsmanIndex++;
                }
            }
        }
        if (currentBatsmanIndex < battingLineup.size()) {
            currentBatters.add(battingLineup.get(currentBatsmanIndex));
        }
        if (currentBatsmanIndex + 1 < battingLineup.size()) {
            currentBatters.add(battingLineup.get(currentBatsmanIndex + 1));
        }
        return currentBatters;
    }

    private String formatOverview(String overview) {
        if (overview == null || overview.trim().isEmpty()) {
            return "No match commentary available.";
        }
        return overview;
    }

    private static class BowlerPerformance {
        final String name;
        final CricketDataParser.BowlingFigures figures;
        BowlerPerformance(String name, CricketDataParser.BowlingFigures figures) {
            this.name = name;
            this.figures = figures;
        }
    }
}