package gui.stages;

import client.Client;
import client.FileOperations;
import client.PlayExistingFile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Helen on 20.04.2017.
 */

class MyCloudStage {
    private Stage stage;
    private Client client;
    private FileOperations fileOperations;
    private HashMap<String, String> parentAndFile;
    private ListView<String> myCloudFilesList;

    void setStage(Stage stage) {
        this.stage = stage;
    }

    MyCloudStage(Client client) {
        this.client = client;
        this.fileOperations = new FileOperations(client.getUsername());

    }

    /**
     * Shows MyCloud page/stage
     */
    void showStage() {
        //Stage settings
        stage.setTitle("AudioHammer");
        int sizeW = 500;
        int sizeH = 300;
        GridPane gridPane = new GridPane();
        stage.setMaxWidth(sizeW);
        stage.setMinWidth(sizeW);
        stage.setMaxHeight(sizeH);
        stage.setMinHeight(sizeH);
        //Files list label
        Label myCloudFilesListLabel = new Label("My files: ");
        //Files list
        this.myCloudFilesList = new ListView<>();
        ObservableList<String> myCloudFiles;
        try {
            myCloudFiles = FXCollections.observableArrayList(myCloudFiles());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        myCloudFilesList.setItems(myCloudFiles);
        System.out.println(myCloudFiles);
        //right-click menu
        ContextMenu cm = new ContextMenu();
        MenuItem rightClickMIListen = new MenuItem("Listen");
        MenuItem rightClickMIDownload = new MenuItem("Download");
        MenuItem rightClickMIRename = new MenuItem("Rename");
        MenuItem rightClickMIDelete = new MenuItem("Delete");
        cm.getItems().addAll(rightClickMIListen, rightClickMIDownload, rightClickMIRename, rightClickMIDelete);
        //consumes right click in right-click menu
        cm.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                event.consume();
            }
        });
        //Reads right-click menu choice
        cm.setOnAction(event -> {
            if ((((MenuItem) event.getTarget()).getText()).equals("Listen")) {
                listenFile(myCloudFilesList.getSelectionModel().getSelectedItem());
            } else if ((((MenuItem) event.getTarget()).getText()).equals("Rename")) {
                renameFileStage(myCloudFilesList.getSelectionModel().getSelectedItem());
            } else if ((((MenuItem) event.getTarget()).getText()).equals("Download")) {
                downloadFile(myCloudFilesList.getSelectionModel().getSelectedItem());
            } else {
                deleteFile(myCloudFilesList.getSelectionModel().getSelectedItem());
            }
        });
        //Shows right-click menu on left click and hides on left-click
        myCloudFilesList.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            String[] targetInfo = e.getTarget().toString().split("'");
            //If right click on filename
            if (e.getButton() == MouseButton.SECONDARY && ((targetInfo.length == 2 && !targetInfo[1].equals("null"))) || (targetInfo.length == 1)) {
                if (myCloudFilesList.getSelectionModel().getSelectedItem() != null) {
                    cm.show(myCloudFilesList, e.getScreenX(), e.getScreenY());
                }
                //If right click on empty space
                else {
                    if (targetInfo.length != 1 && (targetInfo[1].equals("null"))) {
                        myCloudFilesList.getSelectionModel().clearSelection();
                    }
                }
                //If left click
            } else {
                cm.hide();
                //If left click on empty space
                if (myCloudFilesList.getSelectionModel().isSelected(myCloudFilesList.getSelectionModel().getSelectedIndex()) && targetInfo.length >= 2 &&
                        (targetInfo[1].equals("null"))) {
                    myCloudFilesList.getSelectionModel().clearSelection();
                }

            }
        });
        myCloudFilesList.setMaxSize(sizeW - 35, 150);
        myCloudFilesList.setMinSize(sizeW - 35, 150);
        //information label TODO Text editing (Helen)
        Label information = new Label("Here comes some information about chosen file. Date,Length,etc");
        //Back to main stage
        Button backButton = new Button("Back");
        backButton.setOnAction((ActionEvent event) -> {
            mainStage();
        });
        //Adding nodes to grid
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10, 10, 5, 10));
        gridPane.add(myCloudFilesListLabel, 0, 0, 3, 1);
        gridPane.add(myCloudFilesList, 0, 1, 3, 1);
        gridPane.add(information, 0, 2, 3, 1);
        gridPane.add(backButton, 0, 3, 1, 1);

        //Last settings and show
        Scene scene = new Scene(gridPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Makes list of files that user has on server and returns it
     *
     * @return ArrayList<String> with user's files' names. If user does not have any files, returns empty list(not null)
     * @throws IOException
     */
    private ArrayList<String> myCloudFiles() throws IOException {
        ArrayList<Path> allFilesWithPath = fileOperations.getAllFiles();

        //this HashMap is meant for making fileOperations easier
        this.parentAndFile = new HashMap<>();
        for (Path file :
                allFilesWithPath) {
            this.parentAndFile.put(file.getFileName().toString(), file.getParent().toString());
        }
        return new ArrayList<>(parentAndFile.keySet());
    }

    /**
     * Creates popup window that asks for new filename
     *
     * @param fileName the old filename of the file that will be renamed
     */
    private void renameFileStage(String fileName) {
        //New file name popup window
        Stage newFilenameStage = new Stage();
        FlowPane newFilename = new FlowPane();
        newFilename.setStyle("-fx-padding: 10px");
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5, 10, 5, 10));
        //File name inserting
        Label information = new Label("Insert new filename: ");
        TextField newFilenameField = new TextField();
        newFilenameField.setMinWidth(180);
        newFilenameField.setPromptText("Filename");
        //Cancel and confirm buttons
        Button cancel = new Button("Cancel");
        cancel.setOnAction((ActionEvent event) -> {
            newFilenameStage.close();
        });
        Button saveName = new Button("Confirm");
        saveName.setOnAction((ActionEvent event) -> {
            final String newFilenameString = newFilenameField.getText();
            if (renameFile(fileName, newFilenameString)) {
                newFilenameStage.close();
            }

        });
        //Adding nodes to gridpane
        gridPane.add(information, 0, 0, 2, 1);
        gridPane.add(newFilenameField, 0, 1, 2, 1);
        gridPane.add(cancel, 0, 2, 1, 1);
        gridPane.add(saveName, 1, 2, 1, 1);

        //Final setup and show
        Scene scene = new Scene(gridPane, 200, 100);
        newFilenameStage.setScene(scene);
        newFilenameStage.initModality(Modality.APPLICATION_MODAL);
        newFilenameStage.setTitle("Rename file");
        newFilenameStage.showAndWait();
    }

    /**
     * Renames file in server
     *
     * @param oldFilename current filename of the file that will be renamed
     * @param newFilename new filename for the file that will be renamed
     * @return boolean true if filename is changed; false otherwise
     */
    private boolean renameFile(String oldFilename, String newFilename) {
        String oldFile = parentAndFile.get(oldFilename) + File.separator + oldFilename;
        if (fileOperations.renameFile(oldFile, newFilename)) {
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success!");
            success.setHeaderText(null);
            success.setContentText("File succesfully renamed!");
            success.showAndWait();
            refreshTabel();
            return true;
        } else {
            Alert nameExists = new Alert(Alert.AlertType.INFORMATION);
            nameExists.setTitle("Error");
            nameExists.setHeaderText(null);
            nameExists.setContentText("One of your files already has this name!");
            nameExists.showAndWait();
            return false;
        }
    }

    /**
     * Deletes file with given filename from server
     *
     * @param fileName the filename of the file that will be deleted
     */
    private void deleteFile(String fileName) {
        //TODO: ask for confirmation for delete; make list automatically update

        String deleteFile = parentAndFile.get(fileName) + File.separator + fileName;
        fileOperations.deleteFile(deleteFile);
        Alert unassignedButton = new Alert(Alert.AlertType.INFORMATION);
        unassignedButton.setTitle("Success!");
        unassignedButton.setHeaderText(null);
        unassignedButton.setContentText("File " + fileName + " succesfully deleted.");
        unassignedButton.showAndWait();
        refreshTabel();
    }

    /**
     * Plays recorded file with given name from server
     *
     * @param fileName the filename of the file that will be played
     */
    private void listenFile(String fileName) {
        String listenFile = parentAndFile.get(fileName) + File.separator + fileName;
        new Thread(new PlayExistingFile(listenFile)).start();

        //TODO: add custom media player
    }

    /**
     * Downloads file with given filename from server into local device. Will be added later
     *
     * @param fileName the filename of the file that will be downloaded
     */
    private void downloadFile(String fileName) {
        //TODO when fixed, remove alert
        Alert unassignedButton = new Alert(Alert.AlertType.INFORMATION);
        unassignedButton.setTitle("Unassigned!");
        unassignedButton.setHeaderText(null);
        unassignedButton.setContentText("Sorry. This function is not added yet. It can be used in AudioHammer's next stage.");
        unassignedButton.showAndWait();
    }

    /**
     * Switches to Main page/stage
     */
    private void mainStage() {
        MainStage mainStage = new MainStage(client);
        mainStage.setStage(stage);
        mainStage.showStage();

    }

    /**
     * Refreshes Listview list of user serverfiles
     */
    private void refreshTabel() {
        ObservableList<String> myCloudFiles;
        try {
            myCloudFiles = FXCollections.observableArrayList(myCloudFiles());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        myCloudFilesList.setItems(myCloudFiles);
    }
}
