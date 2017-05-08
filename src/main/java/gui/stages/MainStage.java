package gui.stages;

import client.Client;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Constructs a Main stage.
 * Created by Helen on 20.04.2017.
 */
public class MainStage extends BaseStage {
    private Client client;
    private boolean isCreated = false;

    /**
     * isCreated checks if user is connected to server
     *
     * @return true if connection to server is established; false otherwise
     */
    public boolean isCreated() {
        return isCreated;
    }

    MainStage(Client client) {
        this.client = client;
        /*
        try {
            if (!isCreated) {
                client.createConnection();
                this.isCreated = true;
                client.sendCommand("username");
                client.sendCommand(client.getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.INFORMATION); //TODO use
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Could not create a connection. Please try again later.");
            errorAlert.showAndWait();
        }
        */
    }

    /**
     * Shows Main page/stage
     */
    @Override
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
        //General information labels
        Label informationMain = new Label();
        informationMain.setText("Information (Does not change):\nFree MyCloud room:\nLast recording:\n\n\n\n");
        Label informationUser = new Label();
        informationUser.setText("\n2GB\n11.03.2017\n\n\n");
        //Recording stage button
        Button recordingButton = new Button("Recording");
        recordingButton.setOnAction((ActionEvent event) -> {
            switchStage(new RecordingStage(client));
        });
        //MyCloud stage button
        Button myCloudButton = new Button("MyCloud");
        myCloudButton.setOnAction((ActionEvent event) -> {
            switchStage(new MyCloudStage(client));
        });
        //Settings stage button
        Image imageSettings = new Image(getClass().getClassLoader().getResourceAsStream("settings.png"));
        Button settingsButton = new Button("", new ImageView(imageSettings));
        settingsButton.setOnAction((ActionEvent event) -> {
            unassigned();
        });
        //Log out button
        Button logOutButton = new Button("Log out");
        logOutButton.setOnAction((ActionEvent event) -> {
            logOut();
        });

        //Adding nodes to grid
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5, 10, 5, 10));
        gridPane.add(informationMain, 0, 0, 2, 1);
        gridPane.add(informationUser, 2, 0, 2, 1);
        gridPane.add(recordingButton, 0, 1, 1, 1);
        gridPane.add(myCloudButton, 1, 1, 1, 1);
        gridPane.add(settingsButton, 1, 2, 1, 1);
        gridPane.add(logOutButton, 0, 2, 1, 1);
        //Last settings and show
        Scene scene = new Scene(gridPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();


    }

    /**
     * Logs user out of the application
     */
    private void logOut() {
        //TODO logout before LogInStage lines

        //Logout confirmation
        Stage logoutStage = new Stage();
        FlowPane logoutFlowPane = new FlowPane();
        logoutFlowPane.setStyle("-fx-padding: 10px");
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5, 10, 5, 10));
        //File name inserting
        Label information = new Label("Are you sure you want \nto log out from AudioHammer?");
        //Cancel and confirm buttons
        Button noButton = new Button("No");
        noButton.setOnAction((ActionEvent event) -> {
            logoutStage.close();
        });
        Button yesButton = new Button("Yes");
        yesButton.setOnAction((ActionEvent event) -> {
            logoutStage.close();
            switchStage(new LogInStage());
        });
        //Adding nodes to gridpane
        gridPane.add(information, 0, 0, 2, 1);
        gridPane.add(noButton, 0, 2, 1, 1);
        gridPane.add(yesButton, 1, 2, 1, 1);

        //Final setup and show
        Scene scene = new Scene(gridPane, 200, 100);
        logoutStage.setScene(scene);
        logoutStage.initModality(Modality.APPLICATION_MODAL);
        logoutStage.setTitle("Log out");
        logoutStage.showAndWait();

    }

}
