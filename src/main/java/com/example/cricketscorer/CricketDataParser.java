package com.example.cricketscorer;

import java.util.ArrayList;
import java.util.List;

public class CricketDataParser {

    public static List<String> parseBowlingStats(String bowlingLine) {
        List<String> bowlingStats = new ArrayList<>();
        if (bowlingLine == null || bowlingLine.trim().isEmpty()) {
            return bowlingStats;
        }
        String[] segments = bowlingLine.split("(?=\\b[A-Z][a-z]+\\|)");
        for (String segment : segments) {
            segment = segment.trim();
            if (segment.isEmpty()) continue;

            // Find the position of the first "|" after a capital letter name
            int pipeIndex = segment.indexOf('|');
            if (pipeIndex != -1) {
                String bowlerName = segment.substring(0, pipeIndex).trim();
                String ballSequence = segment.substring(pipeIndex + 1).trim();

                if (!bowlerName.isEmpty() && !ballSequence.isEmpty()) {
                    bowlingStats.add(bowlerName + ": " + ballSequence);
                }
            }
        }
        return bowlingStats;
    }

    public static java.util.Map<String, Integer> calculateBattingScores(java.util.List<String> battingLineup, java.util.List<String> bowlingData) {
        java.util.Map<String, Integer> battingScores = new java.util.HashMap<>();
        for (String batsman : battingLineup) {
            battingScores.put(batsman, 0);
        }
        if (battingLineup.isEmpty() || bowlingData.isEmpty()) {
            return battingScores;
        }
        int currentBatsmanIndex = 0;
        String currentBatsman = battingLineup.get(0);
        String nonStriker = battingLineup.size() > 1 ? battingLineup.get(1) : null;
        for (String bowlerStats : bowlingData) {
            if (!bowlerStats.contains(":")) continue;
            String[] parts = bowlerStats.split(":", 2);
            String ballSequence = parts[1].trim();
            String[] balls = ballSequence.split("\\s+");
            for (String ball : balls) {
                ball = ball.trim();
                if (ball.isEmpty()) continue;
                if (ball.equals("wk")) {
                    currentBatsmanIndex++;
                    if (currentBatsmanIndex < battingLineup.size()) {
                        currentBatsman = battingLineup.get(currentBatsmanIndex);
                        if (currentBatsmanIndex + 1 < battingLineup.size() && nonStriker == null) {
                            nonStriker = battingLineup.get(currentBatsmanIndex + 1);
                        }
                    }
                } else if (ball.startsWith("wd") || ball.startsWith("nb") || ball.startsWith("b+")) {
                    if (ball.startsWith("wd+") || ball.startsWith("nb+")) {
                        continue;
                    } else if (ball.startsWith("b+")) {
                        continue;
                    } else if (ball.equals("wd") || ball.equals("nb")) {
                        continue;
                    }
                } else if (ball.matches("\\d+")) {
                    int runs = Integer.parseInt(ball);
                    battingScores.put(currentBatsman, battingScores.get(currentBatsman) + runs);
                    if (runs % 2 == 1 && nonStriker != null) {
                        String temp = currentBatsman;
                        currentBatsman = nonStriker;
                        nonStriker = temp;
                    }
                } else if (ball.contains("+")) {
                    String[] ballParts = ball.split("\\+");
                    if (ballParts.length == 2 && ballParts[0].matches("\\d+")) {
                        int runs = Integer.parseInt(ballParts[0]);
                        battingScores.put(currentBatsman, battingScores.get(currentBatsman) + runs);
                        if (ballParts[1].equals("wk")) {
                            currentBatsmanIndex++;
                            if (currentBatsmanIndex < battingLineup.size()) {
                                currentBatsman = battingLineup.get(currentBatsmanIndex);
                                if (currentBatsmanIndex + 1 < battingLineup.size() && nonStriker == null) {
                                    nonStriker = battingLineup.get(currentBatsmanIndex + 1);
                                }
                            }
                        } else {
                            if (runs % 2 == 1 && nonStriker != null) {
                                String temp = currentBatsman;
                                currentBatsman = nonStriker;
                                nonStriker = temp;
                            }
                        }
                    }
                }
            }
        }
        return battingScores;
    }

    public static BowlingFigures calculateBowlingFigures(String ballSequence) {
        if (ballSequence == null || ballSequence.trim().isEmpty()) {
            return new BowlingFigures(0, 0, 0, 0, 0, 0, 0);
        }
        String[] balls = ballSequence.split("\\s+");
        int bowlerRuns = 0, teamRuns = 0, wickets = 0, ballsBowled = 0, wides = 0, byes = 0, noBalls = 0;
        for (String ball : balls) {
            ball = ball.trim();
            if (ball.isEmpty()) continue;
            if (ball.equals("wk")) {
                wickets++;
                ballsBowled++;
            } else if (ball.equals("wd")) {
                bowlerRuns += 1;
                teamRuns += 1;
                wides++;
            } else if (ball.startsWith("wd+")) {
                String runsStr = ball.substring(3);
                if (runsStr.matches("\\d+")) {
                    int additionalRuns = Integer.parseInt(runsStr);
                    bowlerRuns += 1 + additionalRuns;
                    teamRuns += 1 + additionalRuns;
                    wides++;
                }
            } else if (ball.startsWith("b+")) {
                String runsStr = ball.substring(2);
                if (runsStr.matches("\\d+")) {
                    int byeRuns = Integer.parseInt(runsStr);
                    teamRuns += byeRuns;
                    byes += byeRuns;
                    ballsBowled++;
                }
            } else if (ball.equals("nb")) {
                bowlerRuns += 1;
                teamRuns += 1;
                noBalls++;
            } else if (ball.startsWith("nb+")) {
                String runsStr = ball.substring(3);
                if (runsStr.matches("\\d+")) {
                    int additionalRuns = Integer.parseInt(runsStr);
                    bowlerRuns += 1 + additionalRuns; // All runs against bowler
                    teamRuns += 1 + additionalRuns;
                    noBalls++;
                }
            } else if (ball.matches("\\d+")) {
                int runs = Integer.parseInt(ball);
                bowlerRuns += runs;
                teamRuns += runs;
                ballsBowled++;
            } else if (ball.contains("+")) {
                String[] parts = ball.split("\\+");
                if (parts.length == 2) {
                    if (parts[0].matches("\\d+")) {
                        int runs = Integer.parseInt(parts[0]);
                        bowlerRuns += runs;
                        teamRuns += runs;
                        ballsBowled++;
                    }
                    if (parts[1].equals("wk")) {
                        wickets++;
                    }
                }
            } else if (ball.equals("0") || ball.equals(".")) {
                ballsBowled++;
            } else {
                ballsBowled++;
            }
        }
        return new BowlingFigures(ballsBowled, bowlerRuns, teamRuns, wickets, wides, byes, noBalls);
    }

    public static class BowlingFigures {
        public final int balls;
        public final int bowlerRuns;
        public final int teamRuns;
        public final int wickets;
        public final int wides;
        public final int byes;
        public final int noBalls;

        public BowlingFigures(int balls, int bowlerRuns, int teamRuns, int wickets, int wides, int byes, int noBalls) {
            this.balls = balls;
            this.bowlerRuns = bowlerRuns;
            this.teamRuns = teamRuns;
            this.wickets = wickets;
            this.wides = wides;
            this.byes = byes;
            this.noBalls = noBalls;
        }

        public BowlingFigures(int balls, int runs, int wickets, int wides, int byes) {
            this(balls, runs, runs, wickets, wides, byes, 0);
        }

        public double getOvers() {
            return (balls / 6) + (balls % 6) * 0.1;
        }

        public double getEconomy() {
            return balls > 0 ? (bowlerRuns * 6.0) / balls : 0.0;
        }

        public String getOverString() {
            return (balls / 6) + "." + (balls % 6);
        }

        public int getTotalExtras() {
            return wides + byes + noBalls;
        }

        public int getRuns() {
            return bowlerRuns;
        }

        @Override
        public String toString() {
            return String.format("Overs: %s, Runs: %d, Wickets: %d, Extras: %d (wd:%d, b:%d, nb:%d)",
                    getOverString(), bowlerRuns, wickets, getTotalExtras(), wides, byes, noBalls);
        }
    }
}