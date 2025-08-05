package com.example.cricketscorer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentStorage {
    private static final String MASTER_FILE = "tournaments.txt";

    public static void addTournament(String tournamentName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MASTER_FILE, true))) {
            writer.write(tournamentName + ".txt");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadTournamentFiles() {
        List<String> tournamentFiles = new ArrayList<>();
        File file = new File(MASTER_FILE);
        if (!file.exists()) return tournamentFiles;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tournamentFiles.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tournamentFiles;
    }

    public static Tournament loadTournament(String filename) throws IOException {
        return Tournament.loadFromFile(filename);
    }
}
