package com.example.cricketscorer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Match implements Serializable {
    private String teamAName;
    private String teamBName;
    private int overs; // New field for match overs
    private List<Player> teamA;
    private List<Player> teamB;
    private List<String> teamABatting;
    private List<String> teamBBowling;
    private List<String> teamBBatting;
    private List<String> teamABowling;
    private String overview;

    // New fields for individual batting scores
    private Map<String, Integer> teamABattingScores;
    private Map<String, Integer> teamBBattingScores;

    public Match(String teamAName, String teamBName) {
        this.teamAName = teamAName;
        this.teamBName = teamBName;
        this.overs = 20; // Default overs
        this.teamA = new ArrayList<>();
        this.teamB = new ArrayList<>();
        this.teamABatting = new ArrayList<>();
        this.teamBBowling = new ArrayList<>();
        this.teamBBatting = new ArrayList<>();
        this.teamABowling = new ArrayList<>();
        this.overview = "";
        this.teamABattingScores = new HashMap<>();
        this.teamBBattingScores = new HashMap<>();
    }

    public Match(String teamAName, String teamBName, int overs, List<String> teamABatting, List<String> teamBBowling,
                 List<String> teamBBatting, List<String> teamABowling, String overview) {
        this.teamAName = teamAName;
        this.teamBName = teamBName;
        this.overs = overs;
        this.teamABatting = teamABatting;
        this.teamBBowling = teamBBowling;
        this.teamBBatting = teamBBatting;
        this.teamABowling = teamABowling;
        this.overview = overview;
        this.teamA = new ArrayList<>();
        this.teamB = new ArrayList<>();
        this.teamABattingScores = new HashMap<>();
        this.teamBBattingScores = new HashMap<>();

        // Generate individual batting scores from bowling data
        generateBattingScores();
    }

    // Generate batting scores from bowling data
    public void generateBattingScores() {
        teamABattingScores = CricketDataParser.calculateBattingScores(teamABatting, teamBBowling);
        teamBBattingScores = CricketDataParser.calculateBattingScores(teamBBatting, teamABowling);
    }

    // Calculate team total score from bowling figures
    public TeamScore getTeamAScore() {
        return calculateTeamScore(teamBBowling);
    }

    public TeamScore getTeamBScore() {
        return calculateTeamScore(teamABowling);
    }

    private TeamScore calculateTeamScore(List<String> bowlingData) {
        int totalRuns = 0;
        int totalWickets = 0;

        for (String bowlerStats : bowlingData) {
            if (bowlerStats.contains(":")) {
                String[] parts = bowlerStats.split(":", 2);
                String ballSequence = parts[1].trim();

                CricketDataParser.BowlingFigures figures =
                        CricketDataParser.calculateBowlingFigures(ballSequence);

                totalRuns += figures.teamRuns;  // Use teamRuns instead of runs
                totalWickets += figures.wickets;
            }
        }

        return new TeamScore(totalRuns, totalWickets);
    }
    public boolean finished() {
        TeamScore teamAScore = getTeamAScore();
        TeamScore teamBScore = getTeamBScore();

        // Match is finished if:
        // 1. Team A (batting first) is all out (10 wickets) OR
        // 2. Team B (batting second) is all out (10 wickets) OR
        // 3. Team B (batting second) has chased the target (runs > team A runs) OR
        // 4. Both teams have completed their innings (assuming overs are tracked)

        // Team A all out
        if (teamAScore.wickets >= 10 && teamBScore.wickets >= 10) {
            return true;
        }

        // Team B has chased the target
        if (teamBScore.runs > teamAScore.runs) {
            return true;
        }

        // If both teams have batting data, assume match is finished
        // (This covers cases where overs are completed but wickets < 10)
        double teamAOversCompleted = calculateOversCompleted(teamBBowling);
        double teamBOversCompleted = calculateOversCompleted(teamABowling);

        // Match is finished if both teams have completed their overs
        if (teamAOversCompleted >= overs && teamBOversCompleted >= overs) {
            return true;
        }

        return false;
    }

    // Helper method to calculate completed overs from bowling data
    public double calculateOversCompleted(List<String> bowlingData) {
        int totalBalls = 0;

        for (String bowlerStats : bowlingData) {
            if (bowlerStats != null && bowlerStats.contains(":")) {
                String[] parts = bowlerStats.split(":", 2);
                if (parts.length >= 2) {
                    String ballSequence = parts[1].trim();

                    if (!ballSequence.isEmpty()) {
                        // Split by spaces to get individual balls
                        String[] balls = ballSequence.split("\\s+");

                        for (String ball : balls) {
                            ball = ball.trim();
                            if (!ball.isEmpty()) {
                                // Count all deliveries including extras
                                // In cricket scoring:
                                // - Regular runs (0,1,2,3,4,6) count as valid balls
                                // - Wickets (W) count as valid balls
                                // - Wides and No-balls are extras but still deliveries
                                totalBalls++;
                            }
                        }
                    }
                }
            }
        }

        // Convert total balls to overs
        // 6 balls = 1 over
        // For example: 125 balls = 20.83 overs (20 complete overs + 5 balls)
        return totalBalls / 6.0;
    }

    // Determine match result
    public String getMatchResult() {
        if (!finished()) {
            return "";
        }

        TeamScore teamAScore = getTeamAScore(); // Team A batting first
        TeamScore teamBScore = getTeamBScore(); // Team B batting second

        if (teamAScore.runs > teamBScore.runs) {
            // Team A (batting first) wins by runs
            int margin = teamAScore.runs - teamBScore.runs;
            return teamAName + " won by " + margin + " runs";
        } else if (teamBScore.runs > teamAScore.runs) {
            // Team B (batting second) wins by wickets remaining
            int wicketsRemaining = 10 - teamBScore.wickets;
            return teamBName + " won by " + wicketsRemaining + " wickets";
        } else {
            return "Match Tied";
        }
    }

    // Inner class for team score
    public static class TeamScore {
        public final int runs;
        public final int wickets;

        public TeamScore(int runs, int wickets) {
            this.runs = runs;
            this.wickets = wickets;
        }

        @Override
        public String toString() {
            return runs + "-" + wickets;
        }
    }

    // Getters and setters
    public String getTeamAName() {
        return teamAName;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public int getOvers() {
        return overs;
    }

    public void setOvers(int overs) {
        this.overs = overs;
    }

    public List<Player> getTeamA() {
        return teamA;
    }

    public List<Player> getTeamB() {
        return teamB;
    }

    public void setTeamA(List<Player> teamA) {
        this.teamA = teamA;
    }

    public void setTeamB(List<Player> teamB) {
        this.teamB = teamB;
    }

    public List<String> getBattingA() {
        return teamABatting;
    }

    public List<String> getBattingB() {
        return teamBBatting;
    }

    public List<String> getBowlingA() {
        return teamABowling;
    }

    public List<String> getBowlingB() {
        return teamBBowling;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Map<String, Integer> getTeamABattingScores() {
        return teamABattingScores;
    }

    public Map<String, Integer> getTeamBBattingScores() {
        return teamBBattingScores;
    }

    public void setTeamABattingScores(Map<String, Integer> scores) {
        this.teamABattingScores = scores;
    }

    public void setTeamBBattingScores(Map<String, Integer> scores) {
        this.teamBBattingScores = scores;
    }
    public void setBattingA(List<String> battingA) {
        this.teamABatting = battingA;
    }

    public void setBattingB(List<String> battingB) {
        this.teamBBatting = battingB;
    }

    public void setBowlingA(List<String> bowlingA) {
        this.teamABowling = bowlingA;
    }

    public void setBowlingB(List<String> bowlingB) {
        this.teamBBowling = bowlingB;
    }

    @Override
    public String toString() {
        return teamAName + " vs " + teamBName + " (" + overs + " overs)";
    }

    // Convert the match to a list of strings for writing to file
    public List<String> toFileString() {
        List<String> lines = new ArrayList<>();
        lines.add(teamAName);
        lines.add(teamBName);
        lines.add("Overs: " + overs); // Add overs line

        lines.add(String.join(",", playerNames(teamA)));
        lines.add(String.join(",", playerNames(teamB)));

        lines.add(String.join(",", teamABatting));
        lines.add(String.join(",", teamBBowling));
        lines.add(String.join(",", teamBBatting));
        lines.add(String.join(",", teamABowling));
        lines.add(overview.replace("\n", "\\n")); // Escape newlines
        lines.add("---"); // Separator
        return lines;
    }

    private List<String> playerNames(List<Player> players) {
        List<String> names = new ArrayList<>();
        for (Player p : players) {
            names.add(p.getName()); // assumes Player has getName()
        }
        return names;
    }

    // Load a match from a list of lines
    public static Match fromFileLines(List<String> lines) {
        String teamAName = lines.get(0);
        String teamBName = lines.get(1);

        // Parse overs from line 2
        int overs = 20; // Default value
        String oversLine = lines.get(2);
        if (oversLine.startsWith("Overs: ")) {
            try {
                overs = Integer.parseInt(oversLine.substring(7).trim());
            } catch (NumberFormatException e) {
                // Keep default value if parsing fails
            }
        }

        List<Player> teamA = parsePlayers(lines.get(3));
        List<Player> teamB = parsePlayers(lines.get(4));

        List<String> teamABatting = parseList(lines.get(5));
        List<String> teamBBowling = parseList(lines.get(6));
        List<String> teamBBatting = parseList(lines.get(7));
        List<String> teamABowling = parseList(lines.get(8));
        String overview = lines.get(9).replace("\\n", "\n");

        Match match = new Match(teamAName, teamBName, overs, teamABatting, teamBBowling, teamBBatting, teamABowling, overview);
        match.setTeamA(teamA);
        match.setTeamB(teamB);
        return match;
    }

    private static List<String> parseList(String line) {
        if (line.isEmpty()) return new ArrayList<>();
        String[] parts = line.split(",");
        List<String> list = new ArrayList<>();
        for (String s : parts) {
            list.add(s.trim());
        }
        return list;
    }

    private static List<Player> parsePlayers(String line) {
        List<Player> players = new ArrayList<>();
        for (String name : line.split(",")) {
            players.add(new Player(name.trim())); // assumes Player has constructor Player(String name)
        }
        return players;
    }
}