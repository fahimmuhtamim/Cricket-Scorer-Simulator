package com.example.cricketscorer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SignUp {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private Button signUpButton;

    @FXML
    private Label statusLabel;

    @FXML
    public void handleSignUp() {
        String user = username.getText().trim();
        String pass = password.getText().trim();
        String confirm = confirmPassword.getText().trim();

        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        if (!pass.equals(confirm)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        try (
                BufferedWriter userWriter = new BufferedWriter(new FileWriter("Users.txt", true));
                BufferedWriter passWriter = new BufferedWriter(new FileWriter("Passwords.txt", true))
        ) {
            userWriter.write(user);
            userWriter.newLine();
            passWriter.write(pass);
            passWriter.newLine();
            statusLabel.setStyle("-fx-text-fill: black;");
            statusLabel.setText("Sign up successful!");
            username.clear();
            password.clear();
            confirmPassword.clear();
            signUpButton.setText("Log In");

            signUpButton.setOnAction(e -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("log-in.fxml"));
                    Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

        } catch (IOException e) {
            statusLabel.setText("Error writing to file.");
            e.printStackTrace();
        }
    }
}
