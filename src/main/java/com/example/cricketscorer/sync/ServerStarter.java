package com.example.cricketscorer.sync;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ServerStarter {
    private FileSyncServer server;
    private Thread serverThread;
    private boolean serverRunning = false;

    public ServerStarter() {
        createGUI();
    }

    private void createGUI() {
        JFrame frame = new JFrame("Cricket Scorer Sync Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Status area
        JTextArea statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setBackground(Color.BLACK);
        statusArea.setForeground(Color.GREEN);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(statusArea);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());

        JTextField directoryField = new JTextField("./cricket_data", 20);
        JButton browseButton = new JButton("Browse");
        JButton startButton = new JButton("Start Server");
        JButton stopButton = new JButton("Stop Server");

        stopButton.setEnabled(false);

        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                directoryField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        startButton.addActionListener(e -> {
            String directory = directoryField.getText().trim();
            if (directory.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select a directory");
                return;
            }

            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            server = new FileSyncServer(directory);
            serverThread = new Thread(() -> {
                try {
                    statusArea.append("Starting server on port 8888...\n");
                    statusArea.append("Monitoring directory: " + directory + "\n");
                    server.start();
                } catch (Exception ex) {
                    statusArea.append("Server error: " + ex.getMessage() + "\n");
                }
            });

            serverThread.start();
            serverRunning = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            directoryField.setEnabled(false);
            browseButton.setEnabled(false);
        });

        stopButton.addActionListener(e -> {
            if (server != null) {
                server.stop();
                statusArea.append("Server stopped.\n");
            }
            serverRunning = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            directoryField.setEnabled(true);
            browseButton.setEnabled(true);
        });

        controlPanel.add(new JLabel("Directory:"));
        controlPanel.add(directoryField);
        controlPanel.add(browseButton);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server != null) {
                server.stop();
            }
        }));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerStarter());
    }
}