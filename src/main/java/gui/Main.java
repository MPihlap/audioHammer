package gui;

import javafx.application.Application;
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
        logInStage.setStage(stage);
        logInStage.showStage();
    }
}