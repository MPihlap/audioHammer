package gui.stages;

import client.Client;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import server.LoginHandler;

import java.io.IOException;

/**
 * Created by Helen on 20.04.2017.
 */
public class LogInStage extends Client {
    private Stage stage;
    private Client client;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setClient() {
        this.client = new Client();
    }

    /**
     * Shows LogIn page/stage
     */
    public void showStage() {
        //Stage settings
        stage.setTitle("AudioHammer");
        int sizeW = 250;
        int sizeH = 400;
        GridPane gridPane = new GridPane();
        stage.setMaxWidth(sizeW);
        stage.setMinWidth(sizeW);
        stage.setMaxHeight(sizeH);
        stage.setMinHeight(sizeH);
        //Welcome labels
        Label titleLabel1 = new Label("Welcome to");
        titleLabel1.setFont(Font.font(20));
        Label titleLabel2 = new Label("AUDIOHAMMER");
        titleLabel2.setFont(Font.font(22));
        //Username and password textfields
        Label usernameLabel = new Label("Username: ");
        TextField userNameField = new TextField();
        userNameField.setPromptText("Username");
        userNameField.setMaxWidth(sizeW - 35);
        Label passwordLabel = new Label("Password: ");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(sizeW - 35);
        //Alerts about log in information (password, username)
        Alert noPasswordAlert = new Alert(Alert.AlertType.INFORMATION);
        noPasswordAlert.setTitle("No password!");
        noPasswordAlert.setHeaderText(null);
        noPasswordAlert.setContentText("Please insert your password.");

        Alert noUsernameAlert = new Alert(Alert.AlertType.INFORMATION);
        noUsernameAlert.setTitle("No username!");
        noUsernameAlert.setHeaderText(null);
        noUsernameAlert.setContentText("Please insert your username.");

        Alert wrongUsernameOrPasswordAlert = new Alert(Alert.AlertType.INFORMATION);
        wrongUsernameOrPasswordAlert.setTitle("Wrong username or password!");
        wrongUsernameOrPasswordAlert.setHeaderText(null);
        wrongUsernameOrPasswordAlert.setContentText("You have inserted an incorrect username and/or password.");
        //Log in button
        Button logInButton = new Button("Log in");
        logInButton.setOnAction((ActionEvent event) -> {
            this.setClient(); //starts up new client

            if (userNameField.getText().equals("")) {
                noUsernameAlert.showAndWait();
            } else if (passwordField.getText().equals("")) {
                noPasswordAlert.showAndWait();
            } else {
                try {
                    boolean logInBoolean = logInCheck(passwordField.getText(), userNameField.getText());
                    if (!logInBoolean) {
                        wrongUsernameOrPasswordAlert.showAndWait();
                    } else {
                        MainStage mainStage = new MainStage(client);
                        if (mainStage.isCreated()) {
                            mainStage.setStage(stage);
                            mainStage.showStage();
                        }

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        //Sign up button
        Button signUpButton = new Button("Sign up");
        signUpButton.setOnAction((ActionEvent event) -> {
            signUp();
        });
        //Offline mode button
        Button offlineMode = new Button("Offline mode");
        offlineMode.setMinWidth(sizeW - 35);
        offlineMode.setOnAction((ActionEvent event) -> {
            offlineMode();
        });
        //Adding nodes to grid
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10, 10, 5, 10));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.add(titleLabel1, 0, 0, 3, 1); //TODO horizontally center
        gridPane.add(titleLabel2, 0, 1, 3, 1);//TODO horizontally center
        gridPane.add(offlineMode, 0, 2, 3, 1);
        gridPane.add(usernameLabel, 0, 3, 1, 1);
        gridPane.add(userNameField, 0, 4, 3, 1);
        gridPane.add(passwordLabel, 0, 5, 1, 1);
        gridPane.add(passwordField, 0, 6, 3, 1);
        gridPane.add(logInButton, 0, 7, 1, 1);
        gridPane.add(signUpButton, 1, 7, 1, 1);
        //Last settings and show
        Scene scene = new Scene(gridPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Checks if user can log in with inserted password and username
     *
     * @param password the password that user inserted into PasswordField
     * @param username the username that user inserted into username TextField
     * @return true, if user can log in; false otherwise
     * @throws IOException
     */
    private boolean logInCheck(String password, String username) throws IOException {
        try {
            client.setUsername(username);
            //LogIn and open main window
            return LoginHandler.login(username, password);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Switches to SignUp page/stage
     */
    private void signUp() {
        SignUpStage signUpStage = new SignUpStage();
        signUpStage.setStage(stage);
        signUpStage.showStage();
    }

    /**
     * Allows to use application in offline mode/locally. Will be add later
     */
    private void offlineMode() {
        Alert unassignedButton = new Alert(Alert.AlertType.INFORMATION);
        unassignedButton.setTitle("Unassigned!");
        unassignedButton.setHeaderText(null);
        unassignedButton.setContentText("Sorry. This button is unassigned for now. It can be used in AudioHammer's next stage.");
        unassignedButton.showAndWait();
    }


}
