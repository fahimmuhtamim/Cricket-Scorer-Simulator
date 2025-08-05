package com.example.cricketscorer;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Tournament {
    private String name;
    private LocalDate createdDate;
    private Map<String, List<Player>> teams;
    private List<Match> matches;

    public Tournament(String name) {
        this.name = name;
        this.createdDate = LocalDate.now();
        this.teams = new HashMap<>();
        this.matches = new ArrayList<>();
    }

    public Tournament() {
        this.teams = new HashMap<>();
        this.matches = new ArrayList<>();
    }

    public void createEmptyTeam(String teamName) {
        teams.putIfAbsent(teamName, new ArrayList<>());
    }

    public boolean addPlayerToTeam(String teamName, Player player) {
        List<Player> team = teams.get(teamName);
        if (team != null && team.size() < 11) {
            team.add(player);
            return true;
        }
        return false;
    }

    public void scheduleMatch(String teamA, String teamB) {
        Match m = new Match(teamA, teamB);
        m.setTeamA(teams.getOrDefault(teamA, new ArrayList<>()));
        m.setTeamB(teams.getOrDefault(teamB, new ArrayList<>()));
        matches.add(m);
    }

    public void scheduleMatch(String teamA, String teamB, int overs) {
        Match m = new Match(teamA, teamB);
        m.setOvers(overs);
        m.setTeamA(teams.getOrDefault(teamA, new ArrayList<>()));
        m.setTeamB(teams.getOrDefault(teamB, new ArrayList<>()));
        matches.add(m);
    }

    public void saveToFile(String filename) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            out.println(name);
            out.println("Date: " + createdDate);
            out.println("Teams:");
            for (Map.Entry<String, List<Player>> entry : teams.entrySet()) {
                out.println(entry.getKey());
            }
            out.println("Matches:");
            for (Match m : matches) {
                out.println(m.getTeamAName() + "|" + m.getTeamBName());
                out.println("Overs: " + m.getOvers());
                out.println(String.join("|", m.getTeamA().stream().map(Player::getName).toArray(String[]::new)));
                out.println(String.join("|", m.getTeamB().stream().map(Player::getName).toArray(String[]::new)));
                out.println(String.join("|", m.getBattingA()));
                out.println(String.join(" ", m.getBowlingB()));
                out.println(String.join("|", m.getBattingB()));
                out.println(String.join(" ", m.getBowlingA()));
            }
        }
    }

    public static Tournament loadFromFile(String filename) throws IOException {
        Tournament t = new Tournament();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            t.name = br.readLine();
            String dateLine = br.readLine();
            if (dateLine != null && dateLine.startsWith("Date: ")) {
                t.createdDate = LocalDate.parse(dateLine.substring(6).trim());
            }
            String line;
            while ((line = br.readLine()) != null && !line.equals("Teams:")) {
            }
            while ((line = br.readLine()) != null && !line.equals("Matches:")) {
                if (!line.trim().isEmpty()) {
                    t.createEmptyTeam(line.trim());
                }
            }
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] teamNames = line.split("\\|");
                if (teamNames.length != 2) continue;
                String teamAName = teamNames[0].trim();
                String teamBName = teamNames[1].trim();
                String oversLine = br.readLine();
                int overs = 20; // Default value
                if (oversLine != null && oversLine.startsWith("Overs: ")) {
                    try {
                        overs = Integer.parseInt(oversLine.substring(7).trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
                String teamAPlayers = br.readLine();
                String teamBPlayers = br.readLine();
                String teamABatting = br.readLine();
                String teamBBowling = br.readLine();
                String teamBBatting = br.readLine();
                String teamABowling = br.readLine();
                List<String> teamABattingList = parsePlayerList(teamABatting);
                List<String> teamBBowlingList = parseBowlingStats(teamBBowling);
                List<String> teamBBattingList = parsePlayerList(teamBBatting);
                List<String> teamABowlingList = parseBowlingStats(teamABowling);
                Match match = new Match(teamAName, teamBName, overs, teamABattingList, teamBBowlingList, teamBBattingList, teamABowlingList, "");
                match.setTeamA(parsePlayersToObjects(teamAPlayers));
                match.setTeamB(parsePlayersToObjects(teamBPlayers));
                String overview = generateOverview(match, teamBBowling, teamABowling);
                match.setOverview(overview);
                t.matches.add(match);
            }
        }
        return t;
    }

    private static List<String> parsePlayerList(String line) {
        List<String> result = new ArrayList<>();
        if (line == null || line.trim().isEmpty()) return result;
        String[] players = line.split("\\|");
        for (String player : players) {
            if (!player.trim().isEmpty()) {
                result.add(player.trim());
            }
        }
        return result;
    }

    private static List<String> parseBowlingStats(String line) {
        return CricketDataParser.parseBowlingStats(line);
    }

    private static List<Player> parsePlayersToObjects(String line) {
        List<Player> players = new ArrayList<>();
        if (line == null || line.trim().isEmpty()) return players;
        String[] names = line.split("\\|");
        for (String name : names) {
            if (!name.trim().isEmpty()) {
                players.add(new Player(name.trim()));
            }
        }
        return players;
    }

    private static String generateOverview(Match match, String teamBBowling, String teamABowling) {
        StringBuilder overview = new StringBuilder();
        overview.append("Match Overview: ").append(match.getTeamAName()).append(" vs ").append(match.getTeamBName()).append(" (").append(match.getOvers()).append(" overs each)\n\n");
        overview.append("Result: ").append(match.getMatchResult()).append("\n\n");
        Match.TeamScore teamAScore = match.getTeamAScore();
        Match.TeamScore teamBScore = match.getTeamBScore();
        overview.append("Final Scores:\n");
        overview.append(match.getTeamAName()).append(": ").append(teamAScore.toString()).append("\n");
        overview.append(match.getTeamBName()).append(": ").append(teamBScore.toString()).append("\n\n");
        overview.append("First Innings:\n");
        overview.append(match.getTeamAName()).append(" Batting:\n");
        for (String batter : match.getBattingA()) {
            Integer score = match.getTeamABattingScores().get(batter);
            overview.append("• ").append(batter).append(" - ").append(score != null ? score : 0).append("\n");
        }
        overview.append("\n");
        if (teamBBowling != null && !teamBBowling.isEmpty()) {
            overview.append(match.getTeamBName()).append(" Bowling:\n");
            overview.append(formatBowlingForOverview(teamBBowling));
            overview.append("\n");
        }
        overview.append("Second Innings:\n");
        overview.append(match.getTeamBName()).append(" Batting:\n");
        for (String batter : match.getBattingB()) {
            Integer score = match.getTeamBBattingScores().get(batter);
            overview.append("• ").append(batter).append(" - ").append(score != null ? score : 0).append("\n");
        }
        overview.append("\n");
        if (teamABowling != null && !teamABowling.isEmpty()) {
            overview.append(match.getTeamAName()).append(" Bowling:\n");
            overview.append(formatBowlingForOverview(teamABowling));
        }
        return overview.toString();
    }

    private static String formatBowlingForOverview(String bowlingLine) {
        StringBuilder formatted = new StringBuilder();
        List<String> bowlingStats = CricketDataParser.parseBowlingStats(bowlingLine);
        for (String bowlerStats : bowlingStats) {
            if (bowlerStats.contains(":")) {
                String[] parts = bowlerStats.split(":", 2);
                String bowlerName = parts[0].trim();
                String ballSequence = parts[1].trim();
                CricketDataParser.BowlingFigures figures = CricketDataParser.calculateBowlingFigures(ballSequence);
                formatted.append("• ").append(bowlerName).append(": ").append(figures.getOverString()).append(" overs, ").append(figures.bowlerRuns).append(" runs, ").append(figures.wickets).append(" wickets").append(" (Economy: ").append(String.format("%.2f", figures.getEconomy())).append(")\n");
            }
        }
        return formatted.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public List<String> getTeamNames() {
        return new ArrayList<>(teams.keySet());
    }

    public Map<String, List<Player>> getTeams() {
        return teams;
    }

    public void setTeams(Map<String, List<Player>> teams) {
        this.teams = teams;
    }

    public List<Player> getTeam(String teamName) {
        return teams.get(teamName);
    }

    public void addMatch(Match match) {
        matches.add(match);
    }

    public boolean removeMatch(Match match) {
        return matches.remove(match);
    }

    public int getMatchCount() {
        return matches.size();
    }

    @Override
    public String toString() {
        return name + " (" + createdDate + ") - " + matches.size() + " matches";
    }
}