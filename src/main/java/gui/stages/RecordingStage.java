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

import java.io.IOException;

/**
 * Created by Helen on 18.04.2017.
 */
class RecordingStage {
    private boolean recordingBoolean;
    private long time;
    private TimerThread timerThread;
    private Stage stage;
    private Client client;

    RecordingStage(Client client) {
        this.client = client;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Shows recording page/stage
     */
    void showStage() {
        stage.setTitle("AudioHammer");
        int sizeW = 500;
        int sizeH = 300;
        stage.setMaxWidth(sizeW);
        stage.setMinWidth(sizeW);
        stage.setMaxHeight(sizeH);
        stage.setMinHeight(sizeH);
        recordingBoolean = false;
        //TabPane for buffered recording
        TabPane tabPane = new TabPane();
        Tab recordingTab = new Tab();
        Tab bufferedRecordingTab = new Tab();

        GridPane gridPaneRecording = new GridPane();
        GridPane gridPaneBufferedRecording = new GridPane();
        gridPaneRecording = recordingTab(gridPaneRecording);
        gridPaneBufferedRecording = bufferedRecordingTab(gridPaneBufferedRecording);

        recordingTab.setText("Recording");
        bufferedRecordingTab.setText("Buffered Recording");
        recordingTab.setContent(gridPaneRecording);
        bufferedRecordingTab.setContent(gridPaneBufferedRecording);

        tabPane.getTabs().add(recordingTab);
        tabPane.getTabs().add(bufferedRecordingTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        //Last settings and show
        Scene scene = new Scene(tabPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Makes recording tab's content as a GridPane
     *
     * @param gridPaneRecording the Gridapne on which the tab's content is created
     * @return Given GridPane with added Nodes
     */
    private GridPane recordingTab(GridPane gridPaneRecording) {
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
        Label saveLocation = new Label("File saving location: ");
        CheckBox checkBoxLocal = new CheckBox("Local");
        CheckBox checkBoxCloud = new CheckBox("MyCloud");
        //Recording Start-Stop button
        Button recordingButton = new Button("Start");
        recordingButton.setMinWidth(100);
        recordingButton.setOnAction((ActionEvent event) -> {
            if (checkBoxLocal.isSelected() || checkBoxCloud.isSelected()) {
                if (recordingBoolean) {
                    try {
                        client.stopRecording();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    recordingButton.setText("Start");
                    pauseButton.setDisable(true);
                    pauseButton.setText("Pause");
                    recordingBoolean = false;
                    checkBoxCloud.setDisable(false);
                    checkBoxLocal.setDisable(false);
                    filename.setDisable(false);
                    timerThread.setRecordingBoolean(false);
                } else {
                    if (filename.getText() != null && !filename.getText().equals("") && this.checkFilename(filename.getText())) {
                        try {
                            client.sendCommand("filename");
                            client.sendCommand(filename.getText());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        try {
                            recordingStart();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
            } else {
                noSaveLocationAlert.showAndWait();
            }
        });

        pauseButton.setOnAction((ActionEvent event) -> {
            timerThread.setPauseBoolean(!timerThread.isPauseBoolean());
            if (timerThread.isPauseBoolean()) {
                pauseButton.setText("Resume");
                client.pauseRecording();
            } else {
                pauseButton.setText("Pause");
                client.resumeRecording();
            }
        });

        //Adding nodes to grid
        gridPaneRecording.setVgap(10);
        gridPaneRecording.setHgap(10);
        gridPaneRecording.setPadding(new Insets(5, 10, 5, 10));
        gridPaneRecording.add(fileNameLabel, 0, 0, 3, 1);
        gridPaneRecording.add(filename, 0, 1, 3, 1);
        gridPaneRecording.add(saveLocation, 0, 2, 1, 1);
        gridPaneRecording.add(checkBoxLocal, 1, 2, 1, 1);
        gridPaneRecording.add(checkBoxCloud, 2, 2, 1, 1);
        gridPaneRecording.add(recordingButton, 0, 3, 1, 1);
        gridPaneRecording.add(pauseButton, 1, 3, 1, 1);
        gridPaneRecording.add(timer, 2, 3, 1, 1);
        gridPaneRecording.add(openCloud, 0, 4, 2, 1);
        gridPaneRecording.add(backToMain, 1, 4, 1, 1);
        return gridPaneRecording;
    }

    /**
     * Makes buffered recording tab's content as a GridPane
     *
     * @param gridPaneRecording the Gridapne on which the tab's content is created
     * @return Given GridPane with added Nodes
     */
    private GridPane bufferedRecordingTab(GridPane gridPaneRecording) {
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
        //Recording Pause-Resume button
        Button lapButton = new Button("Save buffer");
        lapButton.setMinWidth(100);
        lapButton.setDisable(!recordingBoolean);
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
        //Saving location checkboxes
        Label saveLocation = new Label("File saving location: ");
        CheckBox checkBoxLocal = new CheckBox("Local");
        CheckBox checkBoxCloud = new CheckBox("MyCloud");
        //Buffertime slider
        Label bufferTimeLabel = new Label("Choose buffertime (min): ");
        Slider bufferTimeSlider = new Slider();
        bufferTimeSlider.setMin(1);
        bufferTimeSlider.setMax(10);
        bufferTimeSlider.setBlockIncrement(1);
        bufferTimeSlider.setValue(1);
        bufferTimeSlider.setShowTickMarks(true);
        bufferTimeSlider.setShowTickLabels(true);
        bufferTimeSlider.setMajorTickUnit(1);
        //Recording Start-Stop button
        Button recordingButton = new Button("Start");
        recordingButton.setMinWidth(100);
        recordingButton.setOnAction((ActionEvent event) -> {
            if (checkBoxLocal.isSelected() || checkBoxCloud.isSelected()) {
                if (recordingBoolean) {
                    //TODO stop recording
                    recordingButton.setText("Start");
                    recordingBoolean = false;
                    bufferTimeSlider.setDisable(false);
                    lapButton.setDisable(true);
                    checkBoxCloud.setDisable(false);
                    checkBoxLocal.setDisable(false);
                    filename.setDisable(false);
                    timerThread.setRecordingBoolean(false);
                } else {
                    if (filename.getText() != null && !filename.getText().equals("") && this.checkFilename(filename.getText())) {
                        try {
                            client.sendCommand("filename");
                            client.sendCommand(filename.getText());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        int bufferTime = (int) bufferTimeSlider.getValue();
                        System.out.println(bufferTime);
                        //TODO start recording
                        timerThread = new TimerThread(timer, time);
                        recordingButton.setText("Stop");
                        bufferTimeSlider.setDisable(true);
                        recordingBoolean = true;
                        lapButton.setDisable(false);
                        timerThread.setRecordingBoolean(true);
                        checkBoxCloud.setDisable(true);
                        checkBoxLocal.setDisable(true);
                        filename.setDisable(true);
                        timerThread.start();
                    } else {
                        noFilenameAlert.showAndWait();
                    }
                }
            } else {
                noSaveLocationAlert.showAndWait();
            }
        });
        lapButton.setOnAction((ActionEvent event) -> {
            //TODO LapAction
            lapAction();
        });
        //Adding nodes to grid
        gridPaneRecording.setVgap(10);
        gridPaneRecording.setHgap(10);
        gridPaneRecording.setPadding(new Insets(5, 10, 5, 10));
        gridPaneRecording.add(fileNameLabel, 0, 0, 3, 1);
        gridPaneRecording.add(filename, 0, 1, 3, 1);
        gridPaneRecording.add(saveLocation, 0, 2, 1, 1);
        gridPaneRecording.add(checkBoxLocal, 1, 2, 1, 1);
        gridPaneRecording.add(checkBoxCloud, 2, 2, 1, 1);
        gridPaneRecording.add(bufferTimeLabel, 0, 3, 1, 1);
        gridPaneRecording.add(bufferTimeSlider, 1, 3, 2, 1);
        gridPaneRecording.add(recordingButton, 0, 4, 1, 1);
        gridPaneRecording.add(lapButton, 1, 4, 1, 1);
        gridPaneRecording.add(timer, 2, 4, 1, 1);
        gridPaneRecording.add(openCloud, 0, 5, 2, 1);
        gridPaneRecording.add(backToMain, 1, 5, 1, 1);
        return gridPaneRecording;
    }

    /**
     * Switches to MyCloud page/stage
     */
    private void myCloudButton() {
        if (recordingBoolean) {
            Alert stillRecordingAlert = new Alert(Alert.AlertType.INFORMATION);
            stillRecordingAlert.setTitle("Still recording!");
            stillRecordingAlert.setHeaderText(null);
            stillRecordingAlert.setContentText("Please stop recording before visiting MyCloud.");
            stillRecordingAlert.showAndWait();
        } else {
            MyCloudStage myCloudStage = new MyCloudStage(client);
            myCloudStage.setStage(stage);
            myCloudStage.showStage();
        }
    }

    /**
     * Starts recording
     *
     * @throws IOException from Client class
     */
    private void recordingStart() throws IOException {
        client.startRecording();
        time = System.currentTimeMillis();
    }

    /**
     * Checks if the failname is allowed
     *
     * @param filename the filename that was inserted into filename TextField
     * @return true if filename is suitable; false if it is not
     */
    // Filename check. TODO 1.add all cases that are not allowed in filename
    private boolean checkFilename(String filename) {
        return !(filename.contains(")") || filename.contains("(")) && !filename.startsWith(".");
    }

    //Saves last n minutes of the recording
    private void lapAction() {

    }

    /**
     * Switches to Main page/stage
     */
    private void mainStage() {
        if (recordingBoolean) {
            Alert stillRecordingAlert = new Alert(Alert.AlertType.INFORMATION);
            stillRecordingAlert.setTitle("Still recording!");
            stillRecordingAlert.setHeaderText(null);
            stillRecordingAlert.setContentText("Please stop recording before returing to Main page.");
            stillRecordingAlert.showAndWait();
        } else {
            MainStage mainStage = new MainStage(client);
            mainStage.setStage(stage);
            mainStage.showStage();
        }
    }
}

