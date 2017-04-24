package gui.stages;

import client.Client;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import server.LoginHandler;

import java.io.File;
import java.io.IOException;

/**
 * Constructs a Sign-up stage.
 * Created by Helen on 20.04.2017.
 */
class SignUpStage extends BaseStage {
    private Client client;

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
        //Account creation problems' alerts TODO add their usage to createAccount();
        Alert usernameInUse = new Alert(Alert.AlertType.INFORMATION); //TODO use
        usernameInUse.setTitle("Username already in use!");
        usernameInUse.setHeaderText(null);
        usernameInUse.setContentText("Sorry, this username is already in use. Please choose another username.");

        Alert passwordTooShort = new Alert(Alert.AlertType.INFORMATION); //TODO use
        passwordTooShort.setTitle("Too short password!");
        passwordTooShort.setHeaderText(null);
        passwordTooShort.setContentText("Password has to be at least 6 characters long. Please choose longer password.");

        Alert passwordsDoNotMatch = new Alert(Alert.AlertType.INFORMATION); //TODO use
        passwordsDoNotMatch.setTitle("Passwords do not match!");
        passwordsDoNotMatch.setHeaderText(null);
        passwordsDoNotMatch.setContentText("These passwords do not match. Try again.");

        Alert unfilledFields = new Alert(Alert.AlertType.INFORMATION);
        unfilledFields.setTitle("Empty fields!");
        unfilledFields.setHeaderText(null);
        unfilledFields.setContentText("Seems like you have left some fields unfilled. Try again.");
        // Create account button
        Button createAccountButton = new Button("Create account");
        createAccountButton.setOnAction((ActionEvent event) -> {
            if (usernameField.getText().equals("") || passwordFieldFirst.getText().equals("") || passwordFieldConfirm.getText().equals("")) {
                unfilledFields.showAndWait();
            } else {
                if (passwordFieldFirst.getText().length() < 6) {
                    passwordTooShort.showAndWait();
                } else if (passwordFieldFirst.getText().equals(passwordFieldConfirm.getText())) {
                    createAccount(usernameField.getText(), passwordFieldFirst.getText());
                } else {
                    passwordsDoNotMatch.showAndWait();
                }
            }
        });
        //Back to lon in stage button
        Button backButton = new Button("Back");
        backButton.setOnAction((ActionEvent event) -> {
            switchPage(new LogInStage());
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
        Alert accountCreated = new Alert(Alert.AlertType.INFORMATION); //TODO use
        accountCreated.setTitle("Success!");
        accountCreated.setHeaderText(null);
        accountCreated.setContentText("New account created!");

        Alert errorAlert = new Alert(Alert.AlertType.INFORMATION); //TODO use
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText("An account with this name already exists!");

        //Account creation before mainStage lines
        try {
            if (LoginHandler.newUserAccount(username, password)) {
                accountCreated.showAndWait();
                this.client = new Client();
                client.setUsername(username);
                (new File(System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + username)).mkdir();

            } else {
                errorAlert.showAndWait();
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        switchPage(new MainStage(client));
    }

}
