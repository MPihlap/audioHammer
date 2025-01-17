package gui;

import client.Client;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import gui.stages.LogInStage;

// When GUI is started, it runs LogInStage as the first thing from Main class
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);

    }

    @Override
    public void start(Stage stage) throws Exception {
        LogInStage logInStage=new LogInStage();
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("logo.png")));
        logInStage.setStage(stage);
        //Client client = new Client();
        logInStage.showStage();
    }
}
