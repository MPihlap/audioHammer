package gui.stages;

import client.Client;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.IOException;

/**
 * Constructs a Sign-up stage.
 * Created by Helen on 20.04.2017.
 */
class SignUpStage extends BaseStage {
    private Client client;

    public SignUpStage(Client client) {
        this.client = client;
    }

    /**
     * Shows SignUp page/stage
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
        //Information labels
        Label generalInformation = new Label("You are creating an account in \nAudioHammer. Please fill all fields.");
        Label usernameLabel = new Label("Choose your username ");
        Label passwordLabel = new Label("Create a password ");
        Label passwordLabel2 = new Label("Confirm your password ");
        //Username and password fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(sizeW - 35);
        PasswordField passwordFieldFirst = new PasswordField();
        passwordFieldFirst.setMaxWidth(sizeW - 35);
        PasswordField passwordFieldConfirm = new PasswordField();
        passwordFieldConfirm.setMaxWidth(sizeW - 35);
        // Create account button
        Button createAccountButton = new Button("Create account");
        createAccountButton.setOnAction((ActionEvent event) -> {
            if (usernameField.getText().equals("") || passwordFieldFirst.getText().equals("") || passwordFieldConfirm.getText().equals("")) {
                alert("Empty fields", "Seems like you have left some fields unfilled. Try again.");
            } else {
                if (passwordFieldFirst.getText().length() < 6) {
                    alert("Too short password!", "Password has to be at least 6 characters long. Please choose longer password.");
                } else if (passwordFieldFirst.getText().equals(passwordFieldConfirm.getText())) {
                    createAccount(usernameField.getText(), passwordFieldFirst.getText());
                } else {
                    alert("Passwords do not match!", "These passwords do not match. Try again.");
                }
            }
        });
        // Enter for username and password fields
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                createAccountButton.fire();
            }
        });
        passwordFieldFirst.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                createAccountButton.fire();
            }
        });
        passwordFieldConfirm.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                createAccountButton.fire();
            }
        });
        //Back to lon in stage button
        Button backButton = new Button("Back");
        backButton.setOnAction((ActionEvent event) -> {
            try {
                client.sendCommand("cancel");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switchStage(new LogInStage());
        });
        //Adding nodes to grid
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10, 10, 5, 5));
        gridPane.add(generalInformation, 0, 0, 1, 1);
        gridPane.add(usernameLabel, 0, 1, 1, 1);
        gridPane.add(usernameField, 0, 2, 1, 1);
        gridPane.add(passwordLabel, 0, 3, 1, 1);
        gridPane.add(passwordFieldFirst, 0, 4, 1, 1);
        gridPane.add(passwordLabel2, 0, 5, 1, 1);
        gridPane.add(passwordFieldConfirm, 0, 6, 1, 1);
        gridPane.add(createAccountButton, 0, 7, 1, 1);
        gridPane.add(backButton, 0, 8, 1, 1);
        //Last settings and show
        Scene scene = new Scene(gridPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Tries to create new account and if successful, show alert about successful account creation, it logs in and switches to Main page/stage. Otherwise shows alert with information.
     *
     * @param username the username that user inserted into new username TextField
     * @param password the password that user inserted into PasswordField
     */
    private void createAccount(String username, String password) {
        //Account creation before mainStage lines
        try {
            if (client.sendUsername(username, password)) {
                alert("Success!", "New account created!");
                client.setUsername(username);
                client.directoryCheck();

            } else {
                alert("Error!", "An account with this name already exists!");
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        switchStage(new MainStage(client));
    }

}
