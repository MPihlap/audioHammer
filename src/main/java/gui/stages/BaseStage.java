package gui.stages;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Provides general methods for all stages
 * Created by Helen on 23.04.2017.
 */
public abstract class BaseStage {
    protected Stage stage;

    /**
     *
     * @param stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     *
     * @param baseStage
     */
    public void switchStage(BaseStage baseStage) {
        stage.setScene(null);
        baseStage.setStage(stage);
        baseStage.showStage();
    }

    /**
     * Generates alert window
     * @param error error text the alert should show
     * @param text futer information text the alert should show
     */
    public void alert(String error, String text) {
        Alert errorAlert = new Alert(Alert.AlertType.INFORMATION);
        errorAlert.setTitle(error);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(text);
        errorAlert.showAndWait();
    }

    public abstract void showStage();
}
