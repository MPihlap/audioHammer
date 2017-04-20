package gui.stages;

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

import java.util.ArrayList;

/**
 * Created by Helen on 20.04.2017.
 */
public class MyCloudStage {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void showStage() {
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
        ListView<String> myCloudFilesList = new ListView<String>();
        ObservableList<String> myCloudFiles = FXCollections.observableArrayList(myCloudFiles());
        myCloudFilesList.setItems(myCloudFiles);
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
            if (e.getButton() == MouseButton.SECONDARY && (!targetInfo[1].equals("null"))&& targetInfo.length>=2) {
                if (myCloudFilesList.getSelectionModel().getSelectedItem() != null) {
                    cm.show(myCloudFilesList, e.getScreenX(), e.getScreenY());
                }
                //If right click on empty space
                else {
                    if ((targetInfo[1].equals("null"))) {
                        myCloudFilesList.getSelectionModel().clearSelection();
                    }
                }
                //If left click
            } else {
                cm.hide();
                //If left click on empty space
                if (myCloudFilesList.getSelectionModel().isSelected(myCloudFilesList.getSelectionModel().getSelectedIndex()) && targetInfo.length>=2&&
                        (targetInfo[1].equals("null"))) {
                    myCloudFilesList.getSelectionModel().clearSelection();
                }

            }
        });
        myCloudFilesList.setMaxSize(sizeW - 35, 150);
        myCloudFilesList.setMinSize(sizeW - 35, 150);
        //information label TODO Text editing (Helen)
        Label information = new Label("Here comes some information about chosen file.Date,Length,etc");
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

    //Makes list of files that user has on cloud and returns it. If user does not have any files, return empty list(not null)
    private ArrayList<String> myCloudFiles() {
        ArrayList<String> myCloudFiles = new ArrayList<>();
        //Just for testing TODO remove later, replace with actual filenames
        myCloudFiles.add("one");
        myCloudFiles.add("two");
        myCloudFiles.add("three");
        //Insert magic
        return myCloudFiles;
    }

    //Asks for new filename
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
            newFilenameStage.close();
            final String newFilenameString = newFilenameField.getText();
            renameFile(fileName, newFilenameString);

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

    //Renames file in server
    private void renameFile(String oldFilename, String newFilename) {
        //TODO file name chancing in server
    }

    private void deleteFile(String fileName) {
        //TODO when fixed, remove alert
        Alert unassignedButton = new Alert(Alert.AlertType.INFORMATION);
        unassignedButton.setTitle("Unassigned!");
        unassignedButton.setHeaderText(null);
        unassignedButton.setContentText("Sorry. This function is not added yet. It can be used in AudioHammer's next stage.");
        unassignedButton.showAndWait();
    }

    //Allows file listeing
    private void listenFile(String fileName) {
        //TODO when fixed, remove alert
        Alert unassignedButton = new Alert(Alert.AlertType.INFORMATION);
        unassignedButton.setTitle("Unassigned!");
        unassignedButton.setHeaderText(null);
        unassignedButton.setContentText("Sorry. This function is not added yet. It can be used in AudioHammer's next stage.");
        unassignedButton.showAndWait();
    }

    //Downloads wawfile.
    private void downloadFile(String fileName) {
        //TODO when fixed, remove alert
        Alert unassignedButton = new Alert(Alert.AlertType.INFORMATION);
        unassignedButton.setTitle("Unassigned!");
        unassignedButton.setHeaderText(null);
        unassignedButton.setContentText("Sorry. This function is not added yet. It can be used in AudioHammer's next stage.");
        unassignedButton.showAndWait();
    }

    private void mainStage() {
        MainStage mainStage = new MainStage();
        mainStage.setStage(stage);
        mainStage.showStage();

    }
}
