package gui.stages;

import client.Client;
import client.FileOperations;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import server.LoginHandler;
import server.PasswordHashing;

import java.io.File;
import java.io.IOException;

/**
 * Constructs a Settings stage.
 * Created by Helen on 20.04.2017.
 */
//TODO
public class SettingsStage extends BaseStage {
    private Client client;

    public SettingsStage(Client client) {
        this.client = client;
    }

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
        //Main label
        Label baseLabel = new Label("Settings");
        baseLabel.setFont(Font.font(20));
        //Change password button
        Button changePassword = new Button("Change password");
        changePassword.setOnAction((ActionEvent event) -> {
            resetPasswordStage();
        });

        //Directory chooser Local
        Label informationDirectoryLocal = new Label("Local recorded files destination:");
        TextField directoryLocalSaves = new TextField();
        directoryLocalSaves.setPromptText("No directory chosen"); //TODO võib panna hetkel valitud kasuta, aga see tuleb siis kuskilt infost fetchida. Sellisel juhul mitte PromptText vaid lihtsalt .setText
        Button chooseDirectoryLocalSaves = new Button("...");
        chooseDirectoryLocalSaves.setOnAction((ActionEvent event) -> {
            String directoryString = directoriChooser();
            directoryLocalSaves.setText(directoryString);
        });
        //Directory choose download
        Label informationDirectoryDownload = new Label("Downloaded files destination:");
        TextField directoryDownload = new TextField();
        directoryDownload.setPromptText("No directory chosen"); //TODO võib panna hetkel valitud kasuta, aga see tuleb siis kuskilt infost fetchida. Sellisel juhul mitte PromptText vaid lihtsalt .setTex
        Button chooseDirectoryDownload = new Button("...");
        chooseDirectoryDownload.setOnAction((ActionEvent event) -> {
            String directoryString = directoriChooser();
            directoryDownload.setText(directoryString);
            
        });
        Button back = new Button("Back");
        back.setOnAction((ActionEvent event) -> { //TODO salvesta valitud kasutad jm vajalik
            String localPath = directoryLocalSaves.getText();
            String downloadPath = directoryDownload.getText();
            switchStage(new MainStage(client));
        });
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5, 10, 5, 10));
        gridPane.add(baseLabel, 0, 0, 2, 1);
        gridPane.add(informationDirectoryLocal, 0, 1, 2, 1);
        gridPane.add(directoryLocalSaves, 0, 2, 1, 1);
        gridPane.add(chooseDirectoryLocalSaves, 1, 2, 1, 1);
        gridPane.add(informationDirectoryDownload, 0, 3, 2, 1);
        gridPane.add(directoryDownload, 0, 4, 1, 1);
        gridPane.add(chooseDirectoryDownload, 1, 4, 1, 1);
        gridPane.add(changePassword, 0, 6, 1, 1);
        gridPane.add(back, 0, 7, 1, 1);

        //Final setup and show
        Scene scene = new Scene(gridPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();
    }

    private String directoriChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            return selectedDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }

    private void resetPasswordStage() {
        Stage resetPasswordStage = new Stage();
        resetPasswordStage.setResizable(false);
        int sizeW = 205;
        int sizeH = 255;
        resetPasswordStage.setMaxWidth(sizeW);
        resetPasswordStage.setMinWidth(sizeW);
        resetPasswordStage.setMaxHeight(sizeH);
        resetPasswordStage.setMinHeight(sizeH);
        FlowPane newFilename = new FlowPane();
        newFilename.setStyle("-fx-padding: 10px");
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5, 10, 5, 10));
        //Password changing
        Label information = new Label("Insert current password: ");
        PasswordField currentPassword = new PasswordField();
        currentPassword.setMinWidth(180);
        currentPassword.setPromptText("Current password");

        Label information2 = new Label("Insert new password: ");
        PasswordField newPasswordOne = new PasswordField();
        newPasswordOne.setMinWidth(180);
        newPasswordOne.setPromptText("New password");

        Label information3 = new Label("Insert new password again: ");
        PasswordField newPasswordTwo = new PasswordField();
        newPasswordTwo.setMinWidth(180);
        newPasswordTwo.setPromptText("New password");

        //Cancel buttons
        Button cancel = new Button("Cancel");
        cancel.setOnAction((ActionEvent event) -> {
            resetPasswordStage.close();
        });
        //Changing password button
        Button changePassword = new Button("Confirm");
        changePassword.setOnAction((ActionEvent event) -> {
            final String currentPasswordInput = currentPassword.getText();
            final String newPasswordInput1 = newPasswordOne.getText();
            final String newPasswordInput2 = newPasswordTwo.getText();
            // TODO Paroolide kontrollimine ja muutmine

            try {
                if(!LoginHandler.login(client.getUsername(), currentPasswordInput)) {
                    alertPasswordChange("You inserted wrong current password! Try again.");
                }
                else if (!newPasswordInput1.equals(newPasswordInput2)){
                    alertPasswordChange("New passwords do not match! Try again.");
                }
                else if(newPasswordInput1.length()<6 || newPasswordInput1.length()==0) {
                    alertPasswordChange("New inserted password is too short!");
                }
                else{
                    if(LoginHandler.changePassword(client.getUsername(), newPasswordInput1)) {
                        alertPasswordChangeConfirm();
                        resetPasswordStage.close();
                    }
                    else {
                        alertPasswordChange("Something went wrong.");
                    }

                }
            } catch (IOException e) {
                alertPasswordChange("You inserted wrong current password! Try again.");
            }
        });
        //Adding nodes to gridpane
        gridPane.add(information, 0, 0, 2, 1);
        gridPane.add(currentPassword, 0, 1, 2, 1);
        gridPane.add(information2, 0, 2, 2, 1);
        gridPane.add(newPasswordOne, 0, 3, 2, 1);
        gridPane.add(information3, 0, 4, 2, 1);
        gridPane.add(newPasswordTwo, 0, 5, 2, 1);
        gridPane.add(cancel, 0, 6, 1, 1);
        gridPane.add(changePassword, 1, 6, 1, 1);

        //Final setup and show
        Scene scene = new Scene(gridPane, 200, 100);
        resetPasswordStage.setScene(scene);
        resetPasswordStage.initModality(Modality.APPLICATION_MODAL);
        resetPasswordStage.setTitle("Change password");
        resetPasswordStage.showAndWait();
    }

    private void alertPasswordChange(String text) {
        Alert nameExists = new Alert(Alert.AlertType.INFORMATION);
        nameExists.setTitle("Error");
        nameExists.setHeaderText(null);
        nameExists.setContentText(text);
        nameExists.showAndWait();
    }

    private void alertPasswordChangeConfirm() {
        Alert nameExists = new Alert(Alert.AlertType.INFORMATION);
        nameExists.setTitle("Success");
        nameExists.setHeaderText(null);
        nameExists.setContentText("Your password was changed successfully");
        nameExists.showAndWait();
    }
}
