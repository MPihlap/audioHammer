package gui.stages;

import client.Client;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.*;
import gui.TimerThread;

/**
 * Created by Helen on 18.04.2017.
 */
public class RecordingStage {
    private boolean recordingBoolean;
    private long time;
    private TimerThread timerThread;
    private Stage stage;
    private Client client;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void showStage() {
        stage.setTitle("AudioHammer");
        int sizeW = 500;
        int sizeH = 300;
        GridPane gridPane = new GridPane();
        stage.setMaxWidth(sizeW);
        stage.setMinWidth(sizeW);
        stage.setMaxHeight(sizeH);
        stage.setMinHeight(sizeH);
        recordingBoolean = false;
        //Filename textfield information label
        final Label fileNameLabel = new Label("Insert filename here: ");
        //Textfield for inserting filename
        TextField filename = new TextField();
        filename.setMinWidth(460);
        filename.setPromptText("Filename...");
        //Button which switch to MyCloud window
        Button openCloud = new Button("MyCloud");
        openCloud.setMinWidth(100);
        openCloud.setOnAction(event -> myCloudButton());
        //Back to main stage button
        Button backToMain = new Button("Back");
        backToMain.setOnAction((ActionEvent event) -> {
            mainStage();
        });
        backToMain.setMinWidth(100);
        //Recording timer (format hh:mm:ss:msms)
        Label timer = new Label("00:00:00");
        timer.setMaxWidth(200);
        //No filename alert
        Alert noFilenameAlert = new Alert(Alert.AlertType.INFORMATION);
        noFilenameAlert.setTitle("No filename!");
        noFilenameAlert.setHeaderText(null);
        noFilenameAlert.setContentText("Please insert filename.");
        //Filename already used alert. TODO add checks and usage
        Alert filenameAlreadyUsedAlert = new Alert(Alert.AlertType.INFORMATION);
        filenameAlreadyUsedAlert.setTitle("Used filename!");
        filenameAlreadyUsedAlert.setHeaderText(null);
        //Choose saving location alert
        Alert noSaveLocationAlert = new Alert(Alert.AlertType.INFORMATION);
        noSaveLocationAlert.setTitle("No saving location!");
        noSaveLocationAlert.setHeaderText(null);
        noSaveLocationAlert.setContentText("Please choose where you wish to save your files.");
        //Recording Pause-Resume button
        Button pauseButton = new Button("Pause");
        pauseButton.setMinWidth(100);
        pauseButton.setDisable(!recordingBoolean);
        //Saving location checkboxes
        Label saveLocation=new Label("File saving location: ");
        CheckBox checkBoxLocal=new CheckBox("Local");
        CheckBox checkBoxCloud=new CheckBox("MyCloud");
        //Recording Start-Stop button
        Button recordingButton = new Button("Start");
        recordingButton.setMinWidth(100);
        recordingButton.setOnAction((ActionEvent event) -> {
            if (checkBoxLocal.isSelected()||checkBoxCloud.isSelected()){
                if (recordingBoolean) {
                    recordingButton.setText("Start");
                    pauseButton.setDisable(true);
                    pauseButton.setText("Pause");
                    recordingBoolean = false;
                    checkBoxCloud.setDisable(false);
                    checkBoxLocal.setDisable(false);
                    filename.setDisable(false);
                    timerThread.setRecordingBoolean(false);
                } else {
                    if (filename.getText() != null && !filename.getText().equals("")) {
                        recordingStart();
                        timerThread = new TimerThread(timer, time);
                        recordingButton.setText("Stop");
                        recordingBoolean = true;
                        timerThread.setRecordingBoolean(true);
                        pauseButton.setDisable(false);
                        checkBoxCloud.setDisable(true);
                        checkBoxLocal.setDisable(true);
                        filename.setDisable(true);
                        timerThread.start();
                    } else {
                        noFilenameAlert.showAndWait();
                    }
                }
            }
            else{
                noSaveLocationAlert.showAndWait();
            }
        });

        pauseButton.setOnAction((ActionEvent event) -> {
            timerThread.setPauseBoolean(!timerThread.isPauseBoolean());
            if (timerThread.isPauseBoolean()) {
                pauseButton.setText("Resume");
            } else {
                pauseButton.setText("Pause");
            }
        });

        //Adding nodes to grid
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5, 10, 5, 10));
        gridPane.add(fileNameLabel, 0, 0, 3, 1);
        gridPane.add(filename, 0, 1, 3, 1);
        gridPane.add(saveLocation,0,2,1,1);
        gridPane.add(checkBoxLocal,1,2,1,1);
        gridPane.add(checkBoxCloud,2,2,1,1);
        gridPane.add(recordingButton, 0, 3, 1, 1);
        gridPane.add(pauseButton, 1, 3, 1, 1);
        gridPane.add(timer, 2, 3, 1, 1);
        gridPane.add(openCloud, 0, 4, 2, 1);
        gridPane.add(backToMain,1,4,1,1);
        //Last settings and show
        Scene scene = new Scene(gridPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();
    }

    // Switches to MyCloud stage
    private void myCloudButton() {
        MyCloudStage myCloudStage=new MyCloudStage();
        myCloudStage.setStage(stage);
        myCloudStage.showStage();
    }

    // Starts recording.
    private void recordingStart() {
        //Add recording start before "time=..."
        time = System.currentTimeMillis();
    }

    // Filename check. TODO 1.add all cases that are not allowed in filename. 2.add checking from already existing files.
    private boolean checkFilename(String filename) {
        return !(filename.contains(")") || filename.contains("(")) && !filename.startsWith(".");
    }

    private void mainStage() {
        MainStage mainStage = new MainStage(client);
        mainStage.setStage(stage);
        mainStage.showStage();
    }
}

