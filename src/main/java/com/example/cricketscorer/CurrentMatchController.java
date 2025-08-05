package com.example.cricketscorer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class CurrentMatchController {

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

    // Scoring buttons
    @FXML private Button wicketBtn;
    @FXML private Button wideBtn;
    @FXML private Button byBtn;
    @FXML private Button noBallBtn;
    @FXML private Button undoBtn;
    @FXML private Button btn0;
    @FXML private Button btn1;
    @FXML private Button btn2;
    @FXML private Button btn3;
    @FXML private Button btn4;
    @FXML private Button btn6;

    // Match state
    private Match currentMatch;
    private String tournamentFile;
    private int currentInnings = 1; // 1 for first innings, 2 for second innings
    private boolean waitingForRunsAfterExtra = false;
    private String pendingExtra = "";

    // Current players
    private String currentBatsman1;
    private String currentBatsman2;
    private String strikeBatsman; // Who's on strike
    private String currentBowler;

    // Match progress tracking
    private List<String> currentBattingLineup;
    private List<String> currentBowlingLineup;
    private Map<String, Integer> currentBattingScores;
    private List<String> currentBowlingStats;
    private int wicketsFallen = 0;
    private int ballsBowled = 0;
    private String lastBowler = "";

    // Choice boxes for player selection
    private ChoiceBox<String> batsmanChoiceBox;
    private ChoiceBox<String> bowlerChoiceBox;
    private boolean needsPlayerSelection = true;

    public void initialize() {
        setupScoringButtons();
        setupPlayerSelection();
        initializeCollections();
    }

    private void initializeCollections() {
        if (currentBattingLineup == null) {
            currentBattingLineup = new ArrayList<>();
        }
        if (currentBowlingLineup == null) {
            currentBowlingLineup = new ArrayList<>();
        }
        if (currentBattingScores == null) {
            currentBattingScores = new HashMap<>();
        }
        if (currentBowlingStats == null) {
            currentBowlingStats = new ArrayList<>();
        }
    }

    public void setMatch(Match match, String tournamentFile) {
        this.currentMatch = match;
        this.tournamentFile = tournamentFile;

        initializeCollections();

        team1Label.setText(match.getTeamAName());
        team2Label.setText(match.getTeamBName());

        loadMatchState();
        determineMatchStateFromData();

        // Show player selection dialog if needed
        if (needsPlayerSelection) {
            showPlayerSelectionDialog();
        }

        updateDisplay();
        updateOverviewSection();
    }

    private void loadMatchState() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(tournamentFile));
            boolean foundMatch = false;
            int lineIndex = 0;

            // Find the current match in the file
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("|") && line.contains(currentMatch.getTeamAName()) &&
                        line.contains(currentMatch.getTeamBName())) {
                    foundMatch = true;
                    lineIndex = i;
                    break;
                }
            }

            if (foundMatch && lineIndex + 7 < lines.size()) {
                // Parse existing match data
                String oversLine = lines.get(lineIndex + 1);
                String teamAPlayers = lines.get(lineIndex + 2);
                String teamBPlayers = lines.get(lineIndex + 3);
                String teamABatting = lines.get(lineIndex + 4);
                String teamBBowling = lines.get(lineIndex + 5);
                String teamBBattingLine = lines.get(lineIndex + 6);
                String teamABowlingLine = lines.get(lineIndex + 7);

                parseAndUpdateMatch(teamAPlayers, teamBPlayers, teamABatting, teamBBowling,
                        teamBBattingLine, teamABowlingLine);
            } else {
                initializeNewMatch();
            }
        } catch (IOException e) {
            initializeNewMatch();
        }
    }

    private void parseAndUpdateMatch(String teamAPlayers, String teamBPlayers,
                                     String teamABatting, String teamBBowling,
                                     String teamBBattingLine, String teamABowlingLine) {

        // Parse team rosters
        List<String> teamAList = Arrays.asList(teamAPlayers.split("\\|"));
        List<String> teamBList = Arrays.asList(teamBPlayers.split("\\|"));

        // Set match teams
        currentMatch.setTeamA(teamAList.stream().map(Player::new).collect(Collectors.toList()));
        currentMatch.setTeamB(teamBList.stream().map(Player::new).collect(Collectors.toList()));

        // Parse first innings data (Team A batting, Team B bowling)
        if (teamABatting != null && !teamABatting.trim().isEmpty()) {
            List<String> teamABattingList = Arrays.asList(teamABatting.split("\\|"));
            currentMatch.setBattingA(teamABattingList);

            List<String> bowlingBStats = CricketDataParser.parseBowlingStats(teamBBowling);
            currentMatch.setBowlingB(bowlingBStats);
            currentMatch.generateBattingScores();
        }

        // Parse second innings data (Team B batting, Team A bowling)
        if (teamBBattingLine != null && !teamBBattingLine.trim().isEmpty()) {
            List<String> teamBBattingList = Arrays.asList(teamBBattingLine.split("\\|"));
            currentMatch.setBattingB(teamBBattingList);

            List<String> bowlingAStats = CricketDataParser.parseBowlingStats(teamABowlingLine);
            currentMatch.setBowlingA(bowlingAStats);
            currentMatch.generateBattingScores();
        }
    }

    private void determineMatchStateFromData() {
        boolean firstInningsHasData = currentMatch.getTeamABattingScores() != null &&
                !currentMatch.getTeamABattingScores().isEmpty();
        boolean secondInningsHasData = currentMatch.getTeamBBattingScores() != null &&
                !currentMatch.getTeamBBattingScores().isEmpty();

        if (firstInningsHasData && !secondInningsHasData) {
            if (isFirstInningsComplete()) {
                currentInnings = 2;
                needsPlayerSelection = true;
            } else {
                currentInnings = 1;
                loadCurrentInningsState();
            }
        } else if (firstInningsHasData && secondInningsHasData) {
            currentInnings = 2;
            loadCurrentInningsState();
        } else if (firstInningsHasData) {
            currentInnings = 1;
            loadCurrentInningsState();
        } else {
            currentInnings = 1;
            needsPlayerSelection = true;
        }
    }

    private boolean isFirstInningsComplete() {
        if (currentMatch.getBowlingB() == null || currentMatch.getBowlingB().isEmpty()) {
            return false;
        }

        double oversCompleted = currentMatch.calculateOversCompleted(currentMatch.getBowlingB());
        Match.TeamScore teamAScore = currentMatch.getTeamAScore();

        return oversCompleted >= currentMatch.getOvers() || teamAScore.wickets >= 10;
    }

    private void loadCurrentInningsState() {
        if (currentInnings == 1) {
            currentBattingLineup = new ArrayList<>(currentMatch.getBattingA());
            currentBowlingStats = convertToCurrentFormat(currentMatch.getBowlingB());
            currentBattingScores = new HashMap<>(currentMatch.getTeamABattingScores());
        } else {
            currentBattingLineup = new ArrayList<>(currentMatch.getBattingB());
            currentBowlingStats = convertToCurrentFormat(currentMatch.getBowlingA());
            currentBattingScores = new HashMap<>(currentMatch.getTeamBBattingScores());
        }

        setCurrentPlayersFromLoadedData();

        if (isOverComplete() || needsNewBowler()) {
            needsPlayerSelection = true;
        } else {
            needsPlayerSelection = false;
        }
    }

    private List<String> convertToCurrentFormat(List<String> bowlingData) {
        List<String> converted = new ArrayList<>();
        for (String stat : bowlingData) {
            if (stat != null && stat.contains(":")) {
                converted.add(stat.replace(":", "|"));
            }
        }
        return converted;
    }

    private void setCurrentPlayersFromLoadedData() {
        if (currentBattingLineup != null && !currentBattingLineup.isEmpty()) {
            currentBatsman1 = currentBattingLineup.get(0);
            if (currentBattingLineup.size() > 1) {
                currentBatsman2 = currentBattingLineup.get(1);
            }
            strikeBatsman = currentBatsman1;
        }

        if (currentBowlingStats != null && !currentBowlingStats.isEmpty()) {
            String lastBowlerStat = currentBowlingStats.get(currentBowlingStats.size() - 1);
            if (lastBowlerStat != null && lastBowlerStat.contains("|")) {
                String[] parts = lastBowlerStat.split("\\|", 2);
                if (parts.length >= 1) {
                    currentBowler = parts[0].trim();
                }
            }
        }

        wicketsFallen = countWicketsFallen();
    }

    private void setupScoringButtons() {
        wicketBtn.setOnAction(e -> handleWicket());
        wideBtn.setOnAction(e -> handleExtra("wd"));
        byBtn.setOnAction(e -> handleExtra("b"));
        noBallBtn.setOnAction(e -> handleExtra("nb"));
        undoBtn.setOnAction(e -> undoLastBall());

        btn0.setOnAction(e -> handleRuns(0));
        btn1.setOnAction(e -> handleRuns(1));
        btn2.setOnAction(e -> handleRuns(2));
        btn3.setOnAction(e -> handleRuns(3));
        btn4.setOnAction(e -> handleRuns(4));
        btn6.setOnAction(e -> handleRuns(6));
    }

    private void setupPlayerSelection() {
        batsmanChoiceBox = new ChoiceBox<>();
        bowlerChoiceBox = new ChoiceBox<>();
    }

    private void initializeNewMatch() {
        currentInnings = 1;
        currentBattingLineup = new ArrayList<>();
        currentBowlingStats = new ArrayList<>();
        currentBattingScores = new HashMap<>();
        wicketsFallen = 0;
        ballsBowled = 0;
        needsPlayerSelection = true;
    }

    private void showPlayerSelectionDialog() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Select Players");
        dialog.setHeaderText("Select current batsmen and bowler");

        ChoiceBox<String> batsman1Choice = new ChoiceBox<>();
        ChoiceBox<String> batsman2Choice = new ChoiceBox<>();
        ChoiceBox<String> bowlerChoice = new ChoiceBox<>();

        if (currentInnings == 1) {
            ObservableList<String> battingTeam = FXCollections.observableArrayList(
                    currentMatch.getTeamA().stream().map(Player::getName).collect(Collectors.toList()));
            ObservableList<String> bowlingTeam = FXCollections.observableArrayList(
                    currentMatch.getTeamB().stream().map(Player::getName).collect(Collectors.toList()));

            batsman1Choice.setItems(battingTeam);
            batsman2Choice.setItems(battingTeam);
            bowlerChoice.setItems(bowlingTeam);
        } else {
            ObservableList<String> battingTeam = FXCollections.observableArrayList(
                    currentMatch.getTeamB().stream().map(Player::getName).collect(Collectors.toList()));
            ObservableList<String> bowlingTeam = FXCollections.observableArrayList(
                    currentMatch.getTeamA().stream().map(Player::getName).collect(Collectors.toList()));

            batsman1Choice.setItems(battingTeam);
            batsman2Choice.setItems(battingTeam);
            bowlerChoice.setItems(bowlingTeam);
        }

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new Label("Batsman 1:"), batsman1Choice,
                new Label("Batsman 2:"), batsman2Choice,
                new Label("Bowler:"), bowlerChoice
        );

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return Arrays.asList(
                        batsman1Choice.getValue(),
                        batsman2Choice.getValue(),
                        bowlerChoice.getValue()
                );
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(players -> {
            currentBatsman1 = players.get(0);
            currentBatsman2 = players.get(1);
            currentBowler = players.get(2);
            strikeBatsman = currentBatsman1;

            if (!currentBattingLineup.contains(currentBatsman1)) {
                currentBattingLineup.add(currentBatsman1);
                currentBattingScores.put(currentBatsman1, 0);
            }
            if (!currentBattingLineup.contains(currentBatsman2)) {
                currentBattingLineup.add(currentBatsman2);
                currentBattingScores.put(currentBatsman2, 0);
            }

            needsPlayerSelection = false;
            updateDisplay();
            updateOverviewSection();
        });
    }

    private void handleExtra(String extraType) {
        if (waitingForRunsAfterExtra) {
            return;
        }

        pendingExtra = extraType;
        waitingForRunsAfterExtra = true;
        highlightRunButtons(true);
    }

    private void handleWicket() {
        if (waitingForRunsAfterExtra) {
            processDelivery(pendingExtra + "+wk");
            resetExtraState();
        } else {
            processDelivery("wk");
        }

        if (wicketsFallen < 10 && getAvailableBatsmen().size() > 0) {
            showNewBatsmanDialog();
        }
    }

    private void handleRuns(int runs) {
        String delivery;

        if (waitingForRunsAfterExtra) {
            if (runs == 0) {
                delivery = pendingExtra;
            } else {
                delivery = pendingExtra + "+" + runs;
            }
            resetExtraState();
        } else {
            delivery = String.valueOf(runs);
        }

        processDelivery(delivery);
    }

    private void processDelivery(String delivery) {
        // Update bowling stats
        updateBowlingStats(delivery);

        // Update batting scores
        updateBattingScores(delivery);

        // Update strike if needed
        updateStrike(delivery);

        // UPDATE THE MATCH OBJECT WITH CURRENT DATA
        updateMatchObject();

        // Save to file
        saveMatchState();

        // Update display AFTER updating match object
        updateDisplay();
        updateOverviewSection();

        // Check for over completion or innings end
        checkOverCompletion();
        checkInningsEnd();
    }

    // NEW METHOD: Update the Match object with current data
    private void updateMatchObject() {
        // Convert current bowling stats to colon format for Match object
        List<String> bowlingForMatch = new ArrayList<>();
        for (String stat : currentBowlingStats) {
            if (stat.contains("|")) {
                bowlingForMatch.add(stat.replace("|", ": "));
            }
        }

        if (currentInnings == 1) {
            // Update first innings data
            currentMatch.setBattingA(new ArrayList<>(currentBattingLineup));
            currentMatch.setBowlingB(bowlingForMatch);
            currentMatch.setTeamABattingScores(new HashMap<>(currentBattingScores));
        } else {
            // Update second innings data
            currentMatch.setBattingB(new ArrayList<>(currentBattingLineup));
            currentMatch.setBowlingA(bowlingForMatch);
            currentMatch.setTeamBBattingScores(new HashMap<>(currentBattingScores));
        }

        // Regenerate batting scores in Match object
        currentMatch.generateBattingScores();
    }

    private void updateBowlingStats(String delivery) {
        if (currentBowlingStats == null) {
            currentBowlingStats = new ArrayList<>();
        }

        boolean found = false;
        for (int i = 0; i < currentBowlingStats.size(); i++) {
            String stat = currentBowlingStats.get(i);
            if (stat != null && stat.contains("|")) {
                String[] parts = stat.split("\\|", 2);
                if (parts.length >= 1) {
                    String bowlerName = parts[0].trim();
                    if (bowlerName.equals(currentBowler)) {
                        currentBowlingStats.set(i, stat + " " + delivery);
                        found = true;
                        break;
                    }
                }
            }
        }

        if (!found) {
            currentBowlingStats.add(currentBowler + "|" + delivery);
        }
    }

    private void updateBattingScores(String delivery) {
        if (delivery.equals("wk") || delivery.contains("+wk")) {
            wicketsFallen++;
            return;
        }

        int runs = 0;
        if (delivery.matches("\\d+")) {
            runs = Integer.parseInt(delivery);
        } else if (delivery.contains("+")) {
            String[] parts = delivery.split("\\+");
            if (parts.length > 1 && parts[1].matches("\\d+")) {
                runs = Integer.parseInt(parts[1]);
            }
        }

        if (runs > 0 && !delivery.startsWith("b") && strikeBatsman != null) {
            currentBattingScores.put(strikeBatsman,
                    currentBattingScores.getOrDefault(strikeBatsman, 0) + runs);
        }
    }

    private void updateStrike(String delivery) {
        if (delivery.matches("\\d+")) {
            int runs = Integer.parseInt(delivery);
            if (runs % 2 == 1) {
                swapStrike();
            }
        } else if (delivery.contains("+")) {
            String[] parts = delivery.split("\\+");
            if (parts.length > 1 && parts[1].matches("\\d+")) {
                int runs = Integer.parseInt(parts[1]);
                if (runs % 2 == 1) {
                    swapStrike();
                }
            }
        }

        if (isOverComplete()) {
            swapStrike();
        }
    }

    private void swapStrike() {
        if (currentBatsman1 != null && currentBatsman2 != null && strikeBatsman != null) {
            strikeBatsman = strikeBatsman.equals(currentBatsman1) ? currentBatsman2 : currentBatsman1;
        }
    }

    private boolean isOverComplete() {
        if (currentBowlingStats == null || currentBowlingStats.isEmpty()) return false;

        String lastBowlerStat = currentBowlingStats.get(currentBowlingStats.size() - 1);
        if (lastBowlerStat != null && lastBowlerStat.contains("|")) {
            String[] parts = lastBowlerStat.split("\\|", 2);
            if (parts.length >= 2) {
                String balls = parts[1].trim();
                CricketDataParser.BowlingFigures figures = CricketDataParser.calculateBowlingFigures(balls);
                return figures.balls % 6 == 0 && figures.balls > 0;
            }
        }
        return false;
    }

    private boolean needsNewBowler() {
        if (currentBowlingStats != null && currentBowlingStats.size() >= 2) {
            String currentBowlerName = currentBowler;
            String secondLastBowlerStat = currentBowlingStats.get(currentBowlingStats.size() - 2);
            if (secondLastBowlerStat != null && secondLastBowlerStat.contains("|")) {
                String[] parts = secondLastBowlerStat.split("\\|", 2);
                if (parts.length >= 1) {
                    String secondLastBowlerName = parts[0].trim();
                    return currentBowlerName != null && currentBowlerName.equals(secondLastBowlerName);
                }
            }
        }
        return false;
    }

    private int countWicketsFallen() {
        int wickets = 0;
        if (currentBowlingStats != null) {
            for (String bowlerStat : currentBowlingStats) {
                if (bowlerStat != null && bowlerStat.contains("|")) {
                    String[] parts = bowlerStat.split("\\|", 2);
                    if (parts.length >= 2) {
                        String balls = parts[1].trim();
                        String[] ballArray = balls.split("\\s+");
                        for (String ball : ballArray) {
                            if ("wk".equals(ball) || ball.endsWith("+wk")) {
                                wickets++;
                            }
                        }
                    }
                }
            }
        }
        return wickets;
    }

    private void checkOverCompletion() {
        if (isOverComplete()) {
            showNewBowlerDialog();
        }
    }

    private void checkInningsEnd() {
        boolean inningsEnd = false;

        if (wicketsFallen >= 10) {
            inningsEnd = true;
        }

        if (currentInnings == 2) {
            Match.TeamScore team1Score = currentMatch.getTeamAScore();
            Match.TeamScore team2Score = currentMatch.getTeamBScore();
            if (team2Score.runs > team1Score.runs) {
                inningsEnd = true;
            }
        }

        double oversCompleted;
        if (currentInnings == 1) {
            oversCompleted = currentMatch.calculateOversCompleted(currentMatch.getBowlingB());
        } else {
            oversCompleted = currentMatch.calculateOversCompleted(currentMatch.getBowlingA());
        }

        if (oversCompleted >= currentMatch.getOvers()) {
            inningsEnd = true;
        }

        if (inningsEnd) {
            if (currentInnings == 1) {
                startSecondInnings();
            } else {
                finishMatch();
            }
        }
    }

    private void startSecondInnings() {
        currentInnings = 2;
        currentBattingLineup = new ArrayList<>();
        currentBowlingStats = new ArrayList<>();
        currentBattingScores = new HashMap<>();
        wicketsFallen = 0;
        needsPlayerSelection = true;
        showPlayerSelectionDialog();
    }

    private void finishMatch() {
        String result = currentMatch.getMatchResult();
        matchResultLabel.setText(result);
        matchResultLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2E8B57;");
        disableScoringButtons();

        // Show match finished dialog
        showMatchFinishedDialog();
    }

    // NEW METHOD: Show match finished dialog
    private void showMatchFinishedDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Match Finished");
        alert.setHeaderText(null);
        alert.setContentText("Match is finished!");

        // Set the dialog as modal
        alert.initModality(Modality.APPLICATION_MODAL);

        // Show and wait for user response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            switchToMatchView();
        }
    }

    private void switchToMatchView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("match-view.fxml"));
            Parent root = loader.load();

            MatchViewController controller = loader.getController();
            controller.setMatch(currentMatch);

            Stage stage = (Stage) team1Label.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Match Summary - " + currentMatch.getTeamAName() + " vs " + currentMatch.getTeamBName());
        } catch (IOException e) {
            // Handle error silently - just stay on current view
        }
    }

    private void showNewBatsmanDialog() {
        List<String> availableBatsmen = getAvailableBatsmen();
        if (availableBatsmen.isEmpty()) return;

        ChoiceDialog<String> dialog = new ChoiceDialog<>(availableBatsmen.get(0), availableBatsmen);
        dialog.setTitle("New Batsman");
        dialog.setHeaderText("Select new batsman");
        dialog.setContentText("Choose batsman:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newBatsman -> {
            if (strikeBatsman != null && strikeBatsman.equals(currentBatsman1)) {
                currentBatsman1 = newBatsman;
            } else {
                currentBatsman2 = newBatsman;
            }
            strikeBatsman = newBatsman;

            if (!currentBattingLineup.contains(newBatsman)) {
                currentBattingLineup.add(newBatsman);
                currentBattingScores.put(newBatsman, 0);
            }

            updateDisplay();
            updateOverviewSection();
        });
    }

    private void showNewBowlerDialog() {
        List<String> availableBowlers = getAvailableBowlers();
        if (availableBowlers.isEmpty()) return;

        ChoiceDialog<String> dialog = new ChoiceDialog<>(availableBowlers.get(0), availableBowlers);
        dialog.setTitle("New Bowler");
        dialog.setHeaderText("Select new bowler");
        dialog.setContentText("Choose bowler:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newBowler -> {
            currentBowler = newBowler;
            lastBowler = currentBowler;
            updateDisplay();
            updateOverviewSection();
        });
    }

    private List<String> getAvailableBatsmen() {
        List<String> teamPlayers;
        if (currentInnings == 1) {
            teamPlayers = currentMatch.getTeamA().stream().map(Player::getName).collect(Collectors.toList());
        } else {
            teamPlayers = currentMatch.getTeamB().stream().map(Player::getName).collect(Collectors.toList());
        }

        return teamPlayers.stream()
                .filter(player -> !currentBattingLineup.contains(player))
                .collect(Collectors.toList());
    }

    private List<String> getAvailableBowlers() {
        List<String> teamPlayers;
        if (currentInnings == 1) {
            teamPlayers = currentMatch.getTeamB().stream().map(Player::getName).collect(Collectors.toList());
        } else {
            teamPlayers = currentMatch.getTeamA().stream().map(Player::getName).collect(Collectors.toList());
        }

        return teamPlayers.stream()
                .filter(player -> !player.equals(lastBowler))
                .collect(Collectors.toList());
    }

    private void saveMatchState() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(tournamentFile));
            boolean foundMatch = false;
            int matchStartLine = -1;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("|") && line.contains(currentMatch.getTeamAName()) &&
                        line.contains(currentMatch.getTeamBName())) {
                    foundMatch = true;
                    matchStartLine = i;
                    break;
                }
            }

            if (foundMatch) {
                String battingLineStr = currentBattingLineup != null ? String.join("|", currentBattingLineup) : "";
                String bowlingLineStr = currentBowlingStats != null ? String.join(" ", currentBowlingStats) : "";

                if (currentInnings == 1) {
                    lines.set(matchStartLine + 4, battingLineStr);
                    lines.set(matchStartLine + 5, bowlingLineStr);
                } else {
                    lines.set(matchStartLine + 6, battingLineStr);
                    lines.set(matchStartLine + 7, bowlingLineStr);
                }

                Files.write(Paths.get(tournamentFile), lines);
            }
        } catch (IOException e) {
            // Handle error silently
        }
    }

    private void updateDisplay() {
        // Update team scores using Match's built-in methods
        Match.TeamScore teamAScore = currentMatch.getTeamAScore();
        Match.TeamScore teamBScore = currentMatch.getTeamBScore();

        team1TotalScore.setText(teamAScore.toString());
        team2TotalScore.setText(teamBScore.toString());

        // Update overs using Match object data
        updateOversDisplay();

        // Update batting and bowling displays
        updateBattingBowlingDisplay();

        // Update match result using Match's built-in method
        if (currentMatch.finished()) {
            matchResultLabel.setText(currentMatch.getMatchResult());
            matchResultLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2E8B57;");
            disableScoringButtons();
        } else {
            matchResultLabel.setText("Match in Progress");
            matchResultLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF6347;");
        }
    }

    private void updateOversDisplay() {
        double team1Overs = currentMatch.calculateOversCompleted(currentMatch.getBowlingB());
        double team2Overs = currentMatch.calculateOversCompleted(currentMatch.getBowlingA());

        team1OversLabel.setText(formatOvers(team1Overs) + "/" + currentMatch.getOvers());
        team2OversLabel.setText(formatOvers(team2Overs) + "/" + currentMatch.getOvers());
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

    // NEW METHOD: Update overview section with match events
    private void updateOverviewSection() {
        StringBuilder overviewText = new StringBuilder();

        try {
            List<String> lines = Files.readAllLines(Paths.get(tournamentFile));
            boolean foundMatch = false;
            int lineIndex = 0;

            // Find the current match in the file
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("|") && line.contains(currentMatch.getTeamAName()) &&
                        line.contains(currentMatch.getTeamBName())) {
                    foundMatch = true;
                    lineIndex = i;
                    break;
                }
            }

            if (foundMatch && lineIndex + 7 < lines.size()) {
                String teamBBowling = lines.get(lineIndex + 5);
                String teamABowling = lines.get(lineIndex + 7);

                // First innings data
                if (teamBBowling != null && !teamBBowling.trim().isEmpty()) {
                    overviewText.append("First Innings: ").append(teamBBowling);
                }

                // Second innings data
                if (teamABowling != null && !teamABowling.trim().isEmpty()) {
                    if (overviewText.length() > 0) {
                        overviewText.append(" ");
                    }
                    overviewText.append("Second Innings: ").append(teamABowling);
                }
            }
        } catch (IOException e) {
            overviewText.append("Unable to load match data");
        }

        // Update the scroll pane content
        Label overviewLabel = new Label(overviewText.toString());
        overviewLabel.setStyle("-fx-font-size: 12px; -fx-padding: 5px;");
        overviewLabel.setWrapText(false);
        overviewScroll.setContent(overviewLabel);
    }

    // FIXED: Simplified display logic without "currently batting/bowling" messages
    private void updateBattingBowlingDisplay() {
        // Clear previous content
        team1BattingVBox.getChildren().clear();
        team2BowlingVBox.getChildren().clear();
        team2BattingVBox.getChildren().clear();
        team1BowlingVBox.getChildren().clear();

        // Check if match data exists
        boolean firstInningsComplete = hasValidInningsData(currentMatch.getTeamABattingScores(), currentMatch.getBowlingB());
        boolean secondInningsComplete = hasValidInningsData(currentMatch.getTeamBBattingScores(), currentMatch.getBowlingA());
        boolean matchFinished = currentMatch.finished();

        if (matchFinished) {
            // Show best 3 batters and bowlers for finished match
            displayBest3Batters(team1BattingVBox, currentMatch.getTeamABattingScores(), currentMatch.getTeamAName());
            displayBest3Batters(team2BattingVBox, currentMatch.getTeamBBattingScores(), currentMatch.getTeamBName());
            displayBest3Bowlers(team2BowlingVBox, currentMatch.getBowlingB(), currentMatch.getTeamBName());
            displayBest3Bowlers(team1BowlingVBox, currentMatch.getBowlingA(), currentMatch.getTeamAName());
        } else {
            if (firstInningsComplete && !secondInningsComplete) {
                // First innings complete, second innings not started or in progress
                displayBest3Batters(team1BattingVBox, currentMatch.getTeamABattingScores(), currentMatch.getTeamAName());
                displayBest3Bowlers(team2BowlingVBox, currentMatch.getBowlingB(), currentMatch.getTeamBName());

                if (currentMatch.getTeamBBattingScores().isEmpty()) {
                    displayEmptyInnings(team2BattingVBox, currentMatch.getTeamBName(), "Yet to bat");
                    displayEmptyInnings(team1BowlingVBox, currentMatch.getTeamAName(), "Yet to bowl");
                } else {
                    displayCurrentBatters(team2BattingVBox, currentMatch.getBattingB(), currentMatch.getTeamBBattingScores(),
                            currentMatch.getBowlingA(), currentMatch.getTeamBName());
                    displayCurrentBowler(team1BowlingVBox, currentMatch.getBowlingA(), currentMatch.getTeamAName());
                }
            } else if (!firstInningsComplete) {
                // First innings in progress
                displayCurrentBatters(team1BattingVBox, currentMatch.getBattingA(), currentMatch.getTeamABattingScores(),
                        currentMatch.getBowlingB(), currentMatch.getTeamAName());
                displayCurrentBowler(team2BowlingVBox, currentMatch.getBowlingB(), currentMatch.getTeamBName());

                displayEmptyInnings(team2BattingVBox, currentMatch.getTeamBName(), "Yet to bat");
                displayEmptyInnings(team1BowlingVBox, currentMatch.getTeamAName(), "Yet to bowl");
            } else {
                // Both innings have data
                displayBest3Batters(team1BattingVBox, currentMatch.getTeamABattingScores(), currentMatch.getTeamAName());
                displayBest3Bowlers(team2BowlingVBox, currentMatch.getBowlingB(), currentMatch.getTeamBName());
                displayCurrentBatters(team2BattingVBox, currentMatch.getBattingB(), currentMatch.getTeamBBattingScores(),
                        currentMatch.getBowlingA(), currentMatch.getTeamBName());
                displayCurrentBowler(team1BowlingVBox, currentMatch.getBowlingA(), currentMatch.getTeamAName());
            }
        }
    }

    private boolean hasValidInningsData(Map<String, Integer> battingScores, List<String> bowlingData) {
        if (battingScores == null || battingScores.isEmpty()) {
            return false;
        }

        if (bowlingData == null || bowlingData.isEmpty()) {
            return false;
        }

        boolean hasRuns = battingScores.values().stream().anyMatch(score -> score > 0);
        boolean hasBalls = bowlingData.stream().anyMatch(data -> {
            if (data != null && data.contains(":")) {
                String[] parts = data.split(":", 2);
                if (parts.length >= 2) {
                    String ballSequence = parts[1].trim();
                    return !ballSequence.isEmpty() && !ballSequence.matches("\\s*");
                }
            }
            return false;
        });

        return hasRuns || hasBalls;
    }

    private void displayEmptyInnings(VBox container, String teamName, String message) {
        container.getChildren().clear();

        Label titleLabel = new Label(teamName);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #808080;");
        container.getChildren().add(titleLabel);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-padding: 10px 0px; -fx-font-size: 12px; -fx-text-fill: #808080; -fx-font-style: italic;");
        container.getChildren().add(messageLabel);
    }

    private void displayBest3Batters(VBox container, Map<String, Integer> battingScores, String teamName) {
        container.getChildren().clear();

        Label titleLabel = new Label(teamName);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2E8B57;");
        container.getChildren().add(titleLabel);

        if (battingScores == null || battingScores.isEmpty()) {
            container.getChildren().add(new Label("No batting data available"));
            return;
        }

        List<Map.Entry<String, Integer>> sortedBatters = battingScores.entrySet().stream()
                .sorted((a, b) -> {
                    int runsComparison = Integer.compare(b.getValue(), a.getValue());
                    if (runsComparison != 0) {
                        return runsComparison;
                    }
                    return a.getKey().compareTo(b.getKey());
                })
                .limit(3)
                .collect(Collectors.toList());

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedBatters) {
            String displayText = rank + ". " + entry.getKey() + " - " + entry.getValue() + " runs";
            Label playerLabel = new Label(displayText);
            playerLabel.setStyle("-fx-padding: 2px 0px; -fx-font-size: 12px;");
            container.getChildren().add(playerLabel);
            rank++;
        }
    }

    private void displayBest3Bowlers(VBox container, List<String> bowlingData, String teamName) {
        container.getChildren().clear();

        Label titleLabel = new Label(teamName);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        container.getChildren().add(titleLabel);

        if (bowlingData == null || bowlingData.isEmpty()) {
            container.getChildren().add(new Label("No bowling data available"));
            return;
        }

        List<BowlerPerformance> bowlerPerformances = new ArrayList<>();
        for (String bowlerStats : bowlingData) {
            if (bowlerStats == null || !bowlerStats.contains(":")) continue;

            String[] parts = bowlerStats.split(":", 2);
            String bowlerName = parts[0].trim();
            String ballSequence = parts[1].trim();

            CricketDataParser.BowlingFigures figures =
                    CricketDataParser.calculateBowlingFigures(ballSequence);

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

    private void displayCurrentBatters(VBox container, List<String> battingLineup,
                                       Map<String, Integer> battingScores, List<String> bowlingData, String teamName) {
        container.getChildren().clear();

        Label titleLabel = new Label(teamName);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF6347;");
        container.getChildren().add(titleLabel);

        if (battingLineup == null || battingLineup.isEmpty()) {
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
            Integer score = battingScores != null ? battingScores.getOrDefault(batter, 0) : 0;
            String displayText = "• " + batter + " - " + score + " runs";
            Label batterLabel = new Label(displayText);
            batterLabel.setStyle("-fx-padding: 2px 0px; -fx-font-size: 12px; -fx-text-fill: #FF6347;");
            container.getChildren().add(batterLabel);
        }
    }

    private void displayCurrentBowler(VBox container, List<String> bowlingData, String teamName) {
        container.getChildren().clear();

        Label titleLabel = new Label(teamName);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF6347;");
        container.getChildren().add(titleLabel);

        if (bowlingData == null || bowlingData.isEmpty()) {
            container.getChildren().add(new Label("No bowling data available"));
            return;
        }

        String currentBowlerStats = bowlingData.get(bowlingData.size() - 1);
        if (currentBowlerStats != null && currentBowlerStats.contains(":")) {
            String[] parts = currentBowlerStats.split(":", 2);
            String bowlerName = parts[0].trim();
            String ballSequence = parts[1].trim();

            CricketDataParser.BowlingFigures figures =
                    CricketDataParser.calculateBowlingFigures(ballSequence);

            String displayText = String.format("• %s - %s-%d-%d (Econ: %.2f)",
                    bowlerName, figures.getOverString(), figures.bowlerRuns,
                    figures.wickets, figures.getEconomy());

            Label bowlerLabel = new Label(displayText);
            bowlerLabel.setStyle("-fx-padding: 2px 0px; -fx-font-size: 12px; -fx-text-fill: #FF6347;");
            container.getChildren().add(bowlerLabel);
        }
    }

    private List<String> getCurrentBatters(List<String> battingLineup, List<String> bowlingData) {
        List<String> currentBatters = new ArrayList<>();

        if (battingLineup == null || battingLineup.isEmpty() || bowlingData == null || bowlingData.isEmpty()) {
            return currentBatters;
        }

        int currentBatsmanIndex = 0;

        for (String bowlerStats : bowlingData) {
            if (!bowlerStats.contains(":")) continue;

            String[] parts = bowlerStats.split(":", 2);
            String ballSequence = parts[1].trim();
            String[] balls = ballSequence.split("\\s+");

            for (String ball : balls) {
                ball = ball.trim();
                if (ball.isEmpty()) continue;

                if (ball.equals("wk") || ball.contains("+wk")) {
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

    private void resetExtraState() {
        waitingForRunsAfterExtra = false;
        pendingExtra = "";
        highlightRunButtons(false);
    }

    private void highlightRunButtons(boolean highlight) {
        String normalStyle = "-fx-text-fill: white; -fx-font-weight: bold;";
        String highlightStyle = normalStyle + " -fx-border-color: yellow; -fx-border-width: 2px;";

        String style = highlight ? highlightStyle : normalStyle;

        btn0.setStyle("-fx-background-color: #808080; " + style);
        btn1.setStyle("-fx-background-color: #4169E1; " + style);
        btn2.setStyle("-fx-background-color: #32CD32; " + style);
        btn3.setStyle("-fx-background-color: #FFD700; -fx-text-fill: black; -fx-font-weight: bold;" +
                (highlight ? " -fx-border-color: yellow; -fx-border-width: 2px;" : ""));
        btn4.setStyle("-fx-background-color: #FF1493; " + style);
        btn6.setStyle("-fx-background-color: #FF0000; " + style);
    }

    private void disableScoringButtons() {
        wicketBtn.setDisable(true);
        wideBtn.setDisable(true);
        byBtn.setDisable(true);
        noBallBtn.setDisable(true);
        undoBtn.setDisable(true);
        btn0.setDisable(true);
        btn1.setDisable(true);
        btn2.setDisable(true);
        btn3.setDisable(true);
        btn4.setDisable(true);
        btn6.setDisable(true);
    }

    // Helper class for bowler performance
    private static class BowlerPerformance {
        final String name;
        final CricketDataParser.BowlingFigures figures;

        BowlerPerformance(String name, CricketDataParser.BowlingFigures figures) {
            this.name = name;
            this.figures = figures;
        }
    }

    // Utility methods for match management
    public void pauseMatch() {
        saveMatchState();
        disableScoringButtons();
    }

    public void resumeMatch() {
        enableScoringButtons();
        updateDisplay();
    }

    private void enableScoringButtons() {
        if (!currentMatch.finished()) {
            wicketBtn.setDisable(false);
            wideBtn.setDisable(false);
            byBtn.setDisable(false);
            noBallBtn.setDisable(false);
            undoBtn.setDisable(false);
            btn0.setDisable(false);
            btn1.setDisable(false);
            btn2.setDisable(false);
            btn3.setDisable(false);
            btn4.setDisable(false);
            btn6.setDisable(false);
        }
    }

    // NEW METHOD: Undo last ball functionality
    public void undoLastBall() {
        if (currentBowlingStats != null && !currentBowlingStats.isEmpty()) {
            String lastBowlerStat = currentBowlingStats.get(currentBowlingStats.size() - 1);
            if (lastBowlerStat != null && lastBowlerStat.contains("|")) {
                String[] parts = lastBowlerStat.split("\\|", 2);
                if (parts.length >= 2) {
                    String bowlerName = parts[0];
                    String balls = parts[1].trim();

                    String[] ballArray = balls.split("\\s+");
                    if (ballArray.length > 1) {
                        // Remove the last ball
                        List<String> ballList = new ArrayList<>(Arrays.asList(ballArray));
                        ballList.remove(ballList.size() - 1);
                        String newBalls = String.join(" ", ballList);
                        currentBowlingStats.set(currentBowlingStats.size() - 1, bowlerName + "|" + newBalls);
                    } else {
                        // Remove the entire bowler entry if it was the only ball
                        currentBowlingStats.remove(currentBowlingStats.size() - 1);
                    }

                    // Recalculate all match state
                    recalculateMatchState();
                    updateMatchObject();
                    saveMatchState();
                    updateDisplay();
                    updateOverviewSection();
                }
            }
        }
    }

    private void recalculateMatchState() {
        if (currentBattingLineup != null && currentBowlingStats != null) {
            List<String> bowlingForParser = new ArrayList<>();
            for (String stat : currentBowlingStats) {
                if (stat.contains("|")) {
                    bowlingForParser.add(stat.replace("|", ": "));
                }
            }

            currentBattingScores = CricketDataParser.calculateBattingScores(
                    currentBattingLineup, bowlingForParser);
        }

        wicketsFallen = countWicketsFallen();

        setCurrentPlayersFromLoadedData();
    }

    public void endMatchManually(String reason) {
        matchResultLabel.setText("Match ended: " + reason);
        matchResultLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #FF6347;");
        disableScoringButtons();
        saveMatchState();
    }

    // Getter methods for external access
    public Match getCurrentMatch() {
        return currentMatch;
    }

    public int getCurrentInnings() {
        return currentInnings;
    }

    public boolean isMatchInProgress() {
        return !currentMatch.finished() && !needsPlayerSelection;
    }

    public String getCurrentScore() {
        if (currentInnings == 1) {
            return currentMatch.getTeamAScore().toString();
        } else {
            return currentMatch.getTeamBScore().toString();
        }
    }

    public String getCurrentOvers() {
        double overs;
        if (currentInnings == 1) {
            overs = currentMatch.calculateOversCompleted(currentMatch.getBowlingB());
        } else {
            overs = currentMatch.calculateOversCompleted(currentMatch.getBowlingA());
        }
        return formatOvers(overs);
    }
}