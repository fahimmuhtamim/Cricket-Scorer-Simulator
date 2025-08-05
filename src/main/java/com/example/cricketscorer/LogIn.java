package com.example.cricketscorer;
import com.sun.tools.javac.Main;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LogIn {
    public LogIn(){

    }
    private Stage stage;
    private Scene scene;
    private Parent root;
    public static boolean viewer;
    @FXML
    private Button logIn;
    @FXML
    private Label wrongLogIn;
    @FXML
    private Button signUp;
    @FXML
    private Button viewerLogin;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    public void userLogin(ActionEvent event) throws IOException{
        checkLogin(event);
    }
    private void checkLogin(ActionEvent e) throws IOException{
        String inputUsername = username.getText().trim();
        String inputPassword = password.getText().trim();

        if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
            wrongLogIn.setText("Please enter your login data.");
            return;
        }

        // Load usernames and passwords
        List<String> usernames = Files.readAllLines(Path.of("Users.txt"));
        List<String> passwords = Files.readAllLines(Path.of("Passwords.txt"));

        for (int i = 0; i < Math.min(usernames.size(), passwords.size()); i++) {
            if (usernames.get(i).trim().equals(inputUsername) &&
                    passwords.get(i).trim().equals(inputPassword)) {
                viewer = false;
                root = FXMLLoader.load(getClass().getResource("tournament-list.fxml"));
                stage = (Stage)((Node)e.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                return;
            }
        }

        wrongLogIn.setText("Wrong Username or Password.");
    }
    public void switchSignUp(ActionEvent event) throws IOException{
        root = FXMLLoader.load(getClass().getResource("sign-up.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void showTournaments(ActionEvent event) throws IOException{
        viewer = true;
        root = FXMLLoader.load(getClass().getResource("tournament-list.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
