package gui.stages;

import client.Client;
import gui.TimerThread;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Constructs a Recording stage.
 * Created by Helen on 18.04.2017.
 */
class RecordingStage extends BaseStage {
    private boolean online;
    private boolean recordingBoolean;
    private long time;
    private TimerThread timerThread;
    private Client client;

    RecordingStage(Client client, boolean online) {
        this.client = client;
        this.online = online;
    }

    /**
     * Shows recording page/stage
     */
    @Override
    public void showStage() {
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
     * @param gridPaneRecording the GridPane on which the tab's content is created
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
        openCloud.setOnAction((ActionEvent event) -> {
            if (recordingBoolean) {
                alert ("Still recording!", "Please stop recording before switching the page.");
            } else {
                try {
                    client.sendCommand("MyCloud");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                switchStage(new MyCloudStage(client));
            }
        });

        //Back button
        Button outButton = new Button("Log in");
        outButton.setMinWidth(100);
        outButton.setOnAction((ActionEvent event) -> {
            LogInStage logInStage = new LogInStage();
            switchStage(logInStage);
        });
        Button backToMain = new Button("Back");
        backToMain.setOnAction((ActionEvent event) -> {
            backCheck();
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
        //Saving location
        TextField directoryLocalSaves = new TextField();
        directoryLocalSaves.setMaxWidth(308);
        directoryLocalSaves.setMinWidth(308);
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String directory = dateTimeFormatter.format(localDate);
        String pathString = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + directory;
        directoryLocalSaves.setText(pathString);
        Button chooseDirectoryLocalSaves = new Button("...");
        chooseDirectoryLocalSaves.setOnAction((ActionEvent event) -> {
            String directoryString = directoriChooser();
            directoryLocalSaves.setText(directoryString);
        });
        Label saveLocation = new Label("File saving location: ");
        CheckBox checkBoxLocal = new CheckBox("Local");
        CheckBox checkBoxCloud = new CheckBox("MyCloud");
        //Recording Start-Stop button
        Button recordingButton = new Button("Start");
        recordingButton.setMinWidth(100);
        recordingButton.setOnAction((ActionEvent event) -> {
            if (checkBoxLocal.isSelected() || checkBoxCloud.isSelected()) {
                client.setSaveLocally(checkBoxLocal.isSelected());
                client.setSaveRemote(checkBoxCloud.isSelected());
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
                        if (online) {
                            client.setFilename(filename.getText());
                        }
                        try {
                            if (!online) {
                                alert("undone", "undone"); //TODO fix local recording
                            } else {
                                recordingStart(filename.getText());
                            }
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
                        new Thread(timerThread).start();
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
        gridPaneRecording.add(pauseButton, 1, 3, 1, 1);
        if (online) {
            gridPaneRecording.add(checkBoxLocal, 1, 2, 1, 1);
            gridPaneRecording.add(checkBoxCloud, 2, 2, 1, 1);
            gridPaneRecording.add(openCloud, 0, 4, 2, 1);
            gridPaneRecording.add(backToMain, 1, 4, 1, 1);
        } else {
            checkBoxLocal.setSelected(true);
            gridPaneRecording.add(directoryLocalSaves, 1, 2, 2, 1);
            gridPaneRecording.add(chooseDirectoryLocalSaves, 3, 2, 1, 1);
            gridPaneRecording.add(outButton, 0, 4, 1, 1);
        }
        gridPaneRecording.add(recordingButton, 0, 3, 1, 1);
        gridPaneRecording.add(timer, 2, 3, 1, 1);
        return gridPaneRecording;
    }

    /**
     * Makes buffered recording tab's content as a GridPane
     *
     * @param gridPaneRecording the GridPane on which the tab's content is created
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
        openCloud.setOnAction((ActionEvent event) -> {
            if (recordingBoolean) {
                alert ("Still recording!", "Please stop recording before switching the page.");
            } else {
                switchStage(new MyCloudStage(client));
            }
        });
        //Back button
        Button outButton = new Button("Log in");
        outButton.setMinWidth(100);
        outButton.setOnAction((ActionEvent event) -> {
            LogInStage logInStage = new LogInStage();
            switchStage(logInStage);
        });
        Button backToMain = new Button("Back");
        backToMain.setOnAction((ActionEvent event) -> {
            backCheck();
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

        //Filename already used alert. TODO add checks and usage
        Alert filenameAlreadyUsedAlert = new Alert(Alert.AlertType.INFORMATION);
        filenameAlreadyUsedAlert.setTitle("Used filename!");
        filenameAlreadyUsedAlert.setHeaderText(null);
        //Saving location
        TextField directoryLocalSaves = new TextField();
        directoryLocalSaves.setMaxWidth(308);
        directoryLocalSaves.setMinWidth(308);
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String directory = dateTimeFormatter.format(localDate);
        String pathString = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + directory;
        directoryLocalSaves.setText(pathString);
        Button chooseDirectoryLocalSaves = new Button("...");
        chooseDirectoryLocalSaves.setOnAction((ActionEvent event) -> {
            String directoryString = directoriChooser();
            directoryLocalSaves.setText(directoryString);
        });
        Label saveLocation = new Label("File saving location: ");
        CheckBox checkBoxLocal = new CheckBox("Local");
        CheckBox checkBoxCloud = new CheckBox("MyCloud");
        //Buffertime slider
        Label bufferTimeLabel = new Label("Buffertime (min): ");
        Slider bufferTimeSlider = new Slider();
        bufferTimeSlider.setMin(1);
        bufferTimeSlider.setMax(10);
        bufferTimeSlider.setValue(1);
        bufferTimeSlider.setShowTickMarks(true);
        bufferTimeSlider.setShowTickLabels(true);
        bufferTimeSlider.setMajorTickUnit(1);
        bufferTimeSlider.setMinorTickCount(0);
        bufferTimeSlider.setSnapToTicks(true);

        //Recording Start-Stop button
        Button recordingButton = new Button("Start");
        recordingButton.setMinWidth(100);
        recordingButton.setOnAction((ActionEvent event) -> {
            if (checkBoxLocal.isSelected() || checkBoxCloud.isSelected()) {
                client.setSaveLocally(checkBoxLocal.isSelected());
                client.setSaveRemote(checkBoxCloud.isSelected());
                if (recordingBoolean) {
                    try {
                        client.stopRecording();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    recordingButton.setText("Start");
                    recordingBoolean = false;
                    bufferTimeSlider.setDisable(false);
                    lapButton.setDisable(true);
                    checkBoxCloud.setDisable(false);
                    checkBoxLocal.setDisable(false);
                    filename.setDisable(false);
                    timerThread.setRecordingBoolean(false);
                } else {
                    System.out.println(bufferTimeSlider.getValue());
                    if (filename.getText() != null && !filename.getText().equals("") && this.checkFilename(filename.getText())) {
                        client.setFilename(filename.getText());
                        int bufferTime = (int) bufferTimeSlider.getValue();
                        System.out.println(bufferTime);
                        try {
                            bufferedRecordingStart(bufferTime, filename.getText());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        timerThread = new TimerThread(timer, time);
                        recordingButton.setText("Stop");
                        bufferTimeSlider.setDisable(true);
                        recordingBoolean = true;
                        lapButton.setDisable(false);
                        timerThread.setRecordingBoolean(true);
                        checkBoxCloud.setDisable(true);
                        checkBoxLocal.setDisable(true);
                        filename.setDisable(true);
                        new Thread(timerThread).start();
                    } else {
                        alert ("No filename!", "Please insert filename");
                    }
                }
            } else {
                alert ("No saving location!", "Please choose where you wish to save your files.");
            }
        });
        lapButton.setOnAction((ActionEvent event) -> {
            try {
                lapAction(bufferTimeSlider);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        //Adding nodes to grid
        gridPaneRecording.setVgap(10);
        gridPaneRecording.setHgap(10);
        gridPaneRecording.setPadding(new Insets(5, 10, 5, 10));
        gridPaneRecording.add(fileNameLabel, 0, 0, 3, 1);
        gridPaneRecording.add(filename, 0, 1, 3, 1);
        gridPaneRecording.add(saveLocation, 0, 2, 1, 1);
        if (online) {
            gridPaneRecording.add(checkBoxLocal, 1, 2, 1, 1);
            gridPaneRecording.add(checkBoxCloud, 2, 2, 1, 1);
            gridPaneRecording.add(openCloud, 0, 5, 2, 1);
            gridPaneRecording.add(backToMain, 1, 5, 1, 1);
        } else {
            gridPaneRecording.add(directoryLocalSaves, 1, 2, 2, 1);
            gridPaneRecording.add(chooseDirectoryLocalSaves, 3, 2, 1, 1);
            gridPaneRecording.add(outButton, 0, 5, 1, 1);

            checkBoxLocal.setSelected(true);
        }
        gridPaneRecording.add(bufferTimeLabel, 0, 3, 1, 1);
        gridPaneRecording.add(bufferTimeSlider, 1, 3, 2, 1);
        gridPaneRecording.add(recordingButton, 0, 4, 1, 1);
        gridPaneRecording.add(lapButton, 1, 4, 1, 1);
        gridPaneRecording.add(timer, 2, 4, 1, 1);
        return gridPaneRecording;
    }

    /**
     * Starts recording
     *
     * @throws IOException from Client class
     */
    private void recordingStart(String filename) throws IOException {
        client.startRecording(filename);
        time = System.currentTimeMillis();
    }

    /**
     * Starts buffered recording saving
     * @param minutes buffered recording time in minutes
     * @param filename file name for buffered file
     * @throws IOException
     */

    private void bufferedRecordingStart(int minutes, String filename) throws IOException {
        client.startBufferedRecording(minutes, filename);
        time = System.currentTimeMillis();
    }

    /**
     * Checks if the failname is allowed
     *
     * @param filename the filename that was inserted into filename TextField
     * @return true if filename is suitable; false if it is not
     */
    private boolean checkFilename(String filename) {
        return !(filename.contains(")") || filename.contains("(")) && !filename.startsWith(".");
    }

    /**
     * Makes a lap in recording
     * @param bufferedTimeSlider Slider for buffertime
     * @throws IOException
     */
    private void lapAction(Slider bufferedTimeSlider) throws IOException {
        client.saveBuffer();
    }

    /**
     * Checks whether it is still recording
     */
    public void backCheck() {
        if (recordingBoolean) {
            alert ("Still recording!", "Please stop recording before switching the page.");
        } else {
            try {
                client.sendCommand("back");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            switchStage(new MainStage(client));
        }
    }

    /**
     * Creates directory chooser
     * @return
     */
    private String directoriChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            return selectedDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }

}

