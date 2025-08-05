package com.example.cricketscorer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TournamentParser {

    public static class Tournament {
        private String name;
        private String date;
        private List<String> teams;
        private List<Match> matches;

        public Tournament() {
            this.teams = new ArrayList<>();
            this.matches = new ArrayList<>();
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public List<String> getTeams() { return teams; }
        public void setTeams(List<String> teams) { this.teams = teams; }
        public List<Match> getMatches() { return matches; }
        public void setMatches(List<Match> matches) { this.matches = matches; }
    }

    public static Tournament parseTournamentFile(String filePath) throws IOException {
        Tournament tournament = new Tournament();
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        int currentIndex = 0;
        if (currentIndex < lines.size()) {
            tournament.setName(lines.get(currentIndex++));
        }
        if (currentIndex < lines.size() && lines.get(currentIndex).startsWith("Date: ")) {
            tournament.setDate(lines.get(currentIndex++).substring(6));
        }
        if (currentIndex < lines.size() && lines.get(currentIndex).equals("Teams:")) {
            currentIndex++;
        }
        List<String> teams = new ArrayList<>();
        while (currentIndex < lines.size() && !lines.get(currentIndex).equals("Matches:")) {
            teams.add(lines.get(currentIndex++));
        }
        tournament.setTeams(teams);
        if (currentIndex < lines.size() && lines.get(currentIndex).equals("Matches:")) {
            currentIndex++;
        }
        List<Match> matches = new ArrayList<>();
        while (currentIndex < lines.size()) {
            Match match = parseMatch(lines, currentIndex);
            if (match != null) {
                matches.add(match);
                currentIndex += getMatchLineCount();
            } else {
                break;
            }
        }
        tournament.setMatches(matches);
        return tournament;
    }

    private static Match parseMatch(List<String> lines, int startIndex) {
        if (startIndex >= lines.size()) return null;
        try {
            String[] teamNames = lines.get(startIndex).split("\\|");
            if (teamNames.length != 2) return null;
            String teamA = teamNames[0].trim();
            String teamB = teamNames[1].trim();
            int overs = 20; // Default
            String oversLine = lines.get(startIndex + 1);
            if (oversLine.startsWith("Overs: ")) {
                try {
                    overs = Integer.parseInt(oversLine.substring(7).trim());
                } catch (NumberFormatException ignored) {
                }
            }
            List<String> teamAPlayers = parsePlayerList(lines.get(startIndex + 2));
            List<String> teamBPlayers = parsePlayerList(lines.get(startIndex + 3));
            List<String> teamABatting = parsePlayerList(lines.get(startIndex + 4));
            List<String> teamBBowling = parseBowlingStats(lines.get(startIndex + 5));
            List<String> teamBBatting = parsePlayerList(lines.get(startIndex + 6));
            List<String> teamABowling = parseBowlingStats(lines.get(startIndex + 7));
            Match match = new Match(teamA, teamB, overs, teamABatting, teamBBowling, teamBBatting, teamABowling, "");
            List<Player> teamAPlayerList = new ArrayList<>();
            for (String name : teamAPlayers) {
                teamAPlayerList.add(new Player(name));
            }
            match.setTeamA(teamAPlayerList);
            List<Player> teamBPlayerList = new ArrayList<>();
            for (String name : teamBPlayers) {
                teamBPlayerList.add(new Player(name));
            }
            match.setTeamB(teamBPlayerList);
            match.setOverview(generateMatchOverview(match));
            return match;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<String> parsePlayerList(String line) {
        List<String> players = new ArrayList<>();
        if (line != null && !line.trim().isEmpty()) {
            String[] names = line.split("\\|");
            for (String name : names) {
                if (!name.trim().isEmpty()) {
                    players.add(name.trim());
                }
            }
        }
        return players;
    }

    private static List<String> parseBowlingStats(String line) {
        return CricketDataParser.parseBowlingStats(line);
    }

    private static int getMatchLineCount() {
        return 8;
    }

    private static String generateMatchOverview(Match match) {
        StringBuilder overview = new StringBuilder();
        overview.append("Match Overview: ").append(match.getTeamAName())
                .append(" vs ").append(match.getTeamBName())
                .append(" (").append(match.getOvers()).append(" overs each)\n\n");
        overview.append(match.getTeamAName()).append(" Batting:\n");
        for (String batter : match.getBattingA()) {
            overview.append("• ").append(batter).append("\n");
        }
        overview.append("\n");
        overview.append(match.getTeamBName()).append(" Bowling:\n");
        for (String bowlerStats : match.getBowlingB()) {
            if (bowlerStats.contains(":")) {
                String[] parts = bowlerStats.split(":", 2);
                String bowlerName = parts[0].trim();
                String ballSequence = parts[1].trim();
                CricketDataParser.BowlingFigures figures =
                        CricketDataParser.calculateBowlingFigures(ballSequence);
                overview.append("• ").append(bowlerName)
                        .append(": ").append(figures.getOverString())
                        .append(" overs, ").append(figures.bowlerRuns).append(" runs, ")
                        .append(figures.wickets).append(" wickets")
                        .append(" (Economy: ").append(String.format("%.2f", figures.getEconomy())).append(")\n");
            }
        }
        overview.append("\n");
        overview.append(match.getTeamBName()).append(" Batting:\n");
        for (String batter : match.getBattingB()) {
            overview.append("• ").append(batter).append("\n");
        }
        overview.append("\n");
        overview.append(match.getTeamAName()).append(" Bowling:\n");
        for (String bowlerStats : match.getBowlingA()) {
            if (bowlerStats.contains(":")) {
                String[] parts = bowlerStats.split(":", 2);
                String bowlerName = parts[0].trim();
                String ballSequence = parts[1].trim();
                CricketDataParser.BowlingFigures figures =
                        CricketDataParser.calculateBowlingFigures(ballSequence);
                overview.append("• ").append(bowlerName)
                        .append(": ").append(figures.getOverString())
                        .append(" overs, ").append(figures.bowlerRuns).append(" runs, ")
                        .append(figures.wickets).append(" wickets")
                        .append(" (Economy: ").append(String.format("%.2f", figures.getEconomy())).append(")\n");
            }
        }
        return overview.toString();
    }
}