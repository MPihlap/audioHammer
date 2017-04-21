package gui.stages;

import client.Client;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;


/**
 * Created by Helen on 20.04.2017.
 */
public class MainStage{
    private Stage stage;
    private Client client;
    private boolean isCreated = false;


    public boolean isCreated() {
        return isCreated;
    }

    public MainStage(Client client) {
        this.client = client;
        try {
            client.createConnection();
            this.isCreated=true;
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.INFORMATION); //TODO use
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Could not create a connection. Please try again later.");
            errorAlert.showAndWait();
        }

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void showStage(){


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
        Label informationMain=new Label();
        informationMain.setText("Information:\nFree MyCloud room:\nLast recording:\n\n\n\n");
        Label informationUser=new Label();
        informationUser.setText("\n2GB\n11.03.2017\n\n\n");
        //Recording stage button
        Button recordingButton=new Button("Recording");
        recordingButton.setOnAction((ActionEvent event)->{
            recording();
        });
        //MyCloud stage button
        Button myCloudButton=new Button("MyCloud");
        myCloudButton.setOnAction((ActionEvent event)->{
            myCloud();
        });
        //Settings stage button
        Image imageSettings = new Image(getClass().getClassLoader().getResourceAsStream("settings.png"));
        Button settingsButton=new Button("",new ImageView(imageSettings));
        settingsButton.setOnAction((ActionEvent event)->{
            settings();
        });
        //Log out button
        Button logOutButton=new Button("Log out");
        logOutButton.setOnAction((ActionEvent event)->{
            logOut();
        });

        //Adding nodes to grid
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5, 10, 5, 10));
        gridPane.add(informationMain, 0, 0, 2, 1);
        gridPane.add(informationUser,2,0,2,1);
        gridPane.add(recordingButton,0,1,1,1);
        gridPane.add(myCloudButton,1,1,1,1);
        gridPane.add(settingsButton,1,2,1,1);
        gridPane.add(logOutButton,0,2,1,1);
        //Last settings and show
        Scene scene = new Scene(gridPane, sizeW, sizeH);
        stage.setScene(scene);
        stage.show();


    }
    //Opens recording stage
    private void settings(){
        Alert unassignedButton = new Alert(Alert.AlertType.INFORMATION);
        unassignedButton.setTitle("Unassigned!");
        unassignedButton.setHeaderText(null);
        unassignedButton.setContentText("Sorry. This button is unassigned for now. It can be used in AudioHammer's next stage.");
        unassignedButton.showAndWait();
    }
    //Opens recording stage
    private void recording(){
        RecordingStage recordingStage=new RecordingStage();
        recordingStage.setStage(stage);
        recordingStage.showStage();

    }
    //Opens myCloud stage
    private void myCloud(){
        MyCloudStage myCloudStage=new MyCloudStage();
        myCloudStage.setStage(stage);
        myCloudStage.showStage();
    }
    //Logging out
    private void logOut(){
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
            LogInStage logInStage=new LogInStage();
            logInStage.setStage(stage);
            logInStage.showStage();
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
