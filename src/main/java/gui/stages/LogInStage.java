package gui.stages;

import client.Client;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Box;

import javax.swing.*;
import java.io.IOException;

/**
 * Constructs a Log-in stage.
 * Created by Helen on 20.04.2017.
 */
public class LogInStage extends BaseStage {
    private Client client;

    private void setClient() {
        this.client = new Client();
    }

    private boolean isInLoginDialogue = false;

    /**
     * Shows LogIn page/stage
     */
    @Override
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
        /*Label titleLabel1 = new Label("Welcome to");
        titleLabel1.setFont(Font.font(20));
        Label titleLabel2 = new Label("AUDIOHAMMER");
        titleLabel2.setFont(Font.font(22));*/
        //Icon
        Label emptyLabel=new Label();
        Label emptyLabel2=new Label();
        Image logoImage=new Image ("logo.png");
        ImageView logo=new ImageView();
        logo.setImage(logoImage);
        logo.setFitWidth(100);
        logo.setPreserveRatio(true);
        logo.setSmooth(true);
        logo.setCache(true);
        //Username and password textfields
        Label usernameLabel = new Label("Username: ");
        TextField userNameField = new TextField();
        userNameField.setPromptText("Username");
        userNameField.setMaxWidth(sizeW - 35);

        Label passwordLabel = new Label("Password: ");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(sizeW - 35);
        //Log in button
        Button logInButton = new Button("Log in");
        this.setClient(); //starts up new client

        logInButton.setOnAction((ActionEvent event) -> {
            try {
                client.createConnection();
                isInLoginDialogue = true;
                if (userNameField.getText().equals("")) {
                    alert("No username!", "Please insert your username.");
                } else if (passwordField.getText().equals("")) {
                    alert("No password", "Please insert your password.");
                } else {
                    try {
                        client.sendCommand("login");
                        boolean logInBoolean = client.sendUsername(userNameField.getText(), passwordField.getText());
                        if (!logInBoolean) {
                            alert("Wrong username or password!", "You have inserted an incorrect username and/or password.");
                        } else {
                            client.setUsername(userNameField.getText());
                            client.directoryCheck();
                            MainStage mainStage = new MainStage(client);
                            switchStage(mainStage);

                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            } catch (IOException e) {
                alert("Error", "Could not create a connection. Please try again later.");
            }
        });
        //Enter for username and Password fields
        userNameField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                logInButton.fire();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                logInButton.fire();
            }
        });
        //Sign up button
        Button signUpButton = new Button("Sign up");
        signUpButton.setOnAction((ActionEvent event) -> {
            if (!client.isSocketCreated()) {
                try {
                    client.createConnection();
                } catch (IOException e) {
                    alert("Error", "Could not create a connection. Please try again later.");
                }
            }
            try {
                if (isInLoginDialogue) {
                    client.sendCommand("cancel");
                }
                client.sendCommand("signup");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switchStage(new SignUpStage(client));
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
        gridPane.add(logo, 0, 0, 3, 1);
        gridPane.setHalignment(logo, HPos.CENTER);
        gridPane.add(offlineMode, 0, 1, 3, 1);
        gridPane.add(usernameLabel, 0, 2, 1, 1);
        gridPane.add(userNameField, 0, 3, 3, 1);
        gridPane.add(passwordLabel, 0, 4, 1, 1);
        gridPane.add(passwordField, 0, 5, 3, 1);
        gridPane.add(logInButton, 0, 6, 1, 1);
        gridPane.add(signUpButton, 1, 6, 1, 1);
        //Last settings and show
        Scene scene = new Scene(gridPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Allows to use application in offline mode/locally.
     */
    private void offlineMode() {
        RecordingStage recordingStage = new RecordingStage(client, false);
        switchStage(recordingStage);
    }


}
