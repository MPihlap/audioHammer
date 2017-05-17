package gui.stages;

import client.Client;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Constructs a Settings stage.
 * Created by Helen on 20.04.2017.
 */
public class SettingsStage extends BaseStage {
    private Client client;

    public SettingsStage(Client client) {
        this.client = client;
    }

    /**
     * Shows Settings page/stage
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
        //Main label
        Label baseLabel = new Label("Settings");
        baseLabel.setFont(Font.font(20));
        //Change password button
        Button changePassword = new Button("Change password");
        changePassword.setOnAction((ActionEvent event) -> {
            resetPasswordStage();
        });
        // Media format button
        Button formatButton = new Button("Format");
        formatButton.minWidth(100);
        formatButton.setOnAction((ActionEvent event) -> {
            chooseFormat();
        });

        //Directory chooser Local
        Label informationDirectoryLocal = new Label("Local recorded files destination:");
        TextField directoryLocalSaves = new TextField();
        directoryLocalSaves.setText(client.getLocalPath());
        Button chooseDirectoryLocalSaves = new Button("...");
        chooseDirectoryLocalSaves.setOnAction((ActionEvent event) -> {
            String directoryString = directoryChooser();
            if (!(directoryString == null)) {
                directoryLocalSaves.setText(directoryString);
            }
        });
        //Directory choose download
        Label informationDirectoryDownload = new Label("Downloaded files destination:");
        TextField directoryDownload = new TextField();
        directoryDownload.setText(client.getDownloadPath());
        Button chooseDirectoryDownload = new Button("...");
        chooseDirectoryDownload.setOnAction((ActionEvent event) -> {
            String directoryString = directoryChooser();
            if (!(directoryString == null)) {
                directoryDownload.setText(directoryString);
            }

        });

        /**
         * changes download and local recording save locations
         */
        Button apply = new Button("Apply");
        apply.setOnAction((ActionEvent event) -> {
            if (!Files.exists(Paths.get(directoryDownload.getText())) ||!Files.exists(Paths.get(directoryLocalSaves.getText()))) {
                alert("Error!", "At least one of the destination folders you have chosen, does not exist. Please choose another location or create the folder.");
            } else {
                String localPath = directoryLocalSaves.getText();
                String downloadPath = directoryDownload.getText();
                if (localPath.equals(null) || downloadPath.equals(null)) {
                    alert("Error", "Please choose a directory for both paths.");
                } else {
                    try {
                        client.updateSettings(localPath, downloadPath);
                    } catch (IOException e) {
                        alert("Error", "Something went wrong. Try again later.");
                    }
                }
            }
        });
        Button back = new Button("Back");
        back.setOnAction((ActionEvent event) -> {
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
        gridPane.add(formatButton, 1, 6, 1, 1);
        gridPane.add(back, 0, 7, 1, 1);
        gridPane.add(apply, 1, 7, 1, 1);

        //Final setup and show
        Scene scene = new Scene(gridPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Creates directory chooser
     */
    private String directoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            return selectedDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Creates reset password stage
     */
    private void resetPasswordStage() {
        Stage resetPasswordStage = new Stage();
        resetPasswordStage.setResizable(false);
        int sizeW = 205;
        int sizeH = 255;
        resetPasswordStage.setMaxWidth(sizeW);
        resetPasswordStage.setMinWidth(sizeW);
        resetPasswordStage.setMaxHeight(sizeH);
        resetPasswordStage.setMinHeight(sizeH);
        FlowPane resetPassword = new FlowPane();
        resetPassword.setStyle("-fx-padding: 10px");
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

            try {
                if (!LoginHandler.login(client.getUsername(), currentPasswordInput)) {
                    alert("Error", "You inserted wrong current password! Try again.");
                } else if (!newPasswordInput1.equals(newPasswordInput2)) {
                    alert("Error", "New passwords do not match! Try again.");
                } else if (newPasswordInput1.length() < 6 || newPasswordInput1.length() == 0) {
                    alert("Error", "New inserted password is too short!");
                } else {
                    if (client.passwordChange(newPasswordInput1)) {
                        alert("Success", "Your password was changed successfully");
                        resetPasswordStage.close();
                    } else {
                        alert("Error", "Something went wrong.");
                    }

                }
            } catch (IOException e) {
                alert("Error", "You inserted wrong current password! Try again.");
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

    /**
     * Creates and shows format stage
     */
    private void chooseFormat() {
        Stage chooseFormat = new Stage();
        chooseFormat.setResizable(false);
        int sizeW = 350;
        int sizeH = 160;
        chooseFormat.setMaxWidth(sizeW);
        chooseFormat.setMinWidth(sizeW);
        chooseFormat.setMaxHeight(sizeH);
        chooseFormat.setMinHeight(sizeH);
        FlowPane chooseFormatFlow = new FlowPane();
        chooseFormatFlow.setStyle("-fx-padding: 10px");
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5, 10, 5, 10));
        //Main Label
        Label mainInformation = new Label("Choose format parameters: ");
        //Format options
        Label information1 = new Label("Sample rate:");
        TextField sampleRateField = new TextField();
        sampleRateField.setMinWidth(100);
        sampleRateField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    sampleRateField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        Label information2 = new Label("Sample size:");
        TextField sampleSizeField = new TextField();
        sampleSizeField.setMinWidth(100);
        sampleSizeField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    sampleSizeField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        Label information3 = new Label("Channels:");
        TextField channelsField = new TextField();
        channelsField.setMinWidth(100);
        channelsField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    channelsField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        //Cancel buttons
        Button cancel = new Button("Cancel");
        cancel.setOnAction((ActionEvent event) -> {
            chooseFormat.close();
        });
        Button confirm = new Button("Confirm");
        confirm.setOnAction((ActionEvent event) -> {
            int sampleRate = Integer.parseInt(sampleRateField.getText());
            int sampleSize = Integer.parseInt(sampleSizeField.getText());
            int channels = Integer.parseInt(channelsField.getText());
            client.setAudioFormat(new AudioFormat(sampleRate,sampleSize,channels,true,true));

            // TODO set filesaving parameters
        });
        //Adding nodes to gridpane
        gridPane.add(mainInformation, 0, 0, 3, 1);
        gridPane.add(information1, 0, 1, 1, 1);
        gridPane.add(information2, 1, 1, 1, 1);
        gridPane.add(information3, 2, 1, 1, 1);
        gridPane.add(sampleRateField, 0, 2, 1, 1);
        gridPane.add(sampleSizeField, 2, 2, 1, 1);
        gridPane.add(channelsField, 1, 2, 1, 1);
        gridPane.add(cancel, 0, 3, 1, 1);
        gridPane.add(confirm, 1, 3, 1, 1);

        //Final setup and show
        Scene scene = new Scene(gridPane, 200, 100);
        chooseFormat.setScene(scene);
        chooseFormat.initModality(Modality.APPLICATION_MODAL);
        chooseFormat.setTitle("Choose format");
        chooseFormat.showAndWait();
    }
}
