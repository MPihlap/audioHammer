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
     * Connects created Stage-class (for example LogInStage) with base stage
     * @param stage base-stage that is changed for different pages
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Changes given basestage for switching to different page
     * @param baseStage base-stage that needs changing
     */
    public void switchPage(BaseStage baseStage) {
        stage.setScene(null);
        baseStage.setStage(stage);
        baseStage.showStage();
    }

    /**
     * shows Alert if user tries to use functionality which is unavailable
     */
    void unassigned() {
        Alert unassignedButton = new Alert(Alert.AlertType.INFORMATION);
        unassignedButton.setTitle("Unassigned!");
        unassignedButton.setHeaderText(null);
        unassignedButton.setContentText("Sorry. This button is unassigned for now. It can be used in AudioHammer's next stage.");
        unassignedButton.showAndWait();
    }

    /**
     * Shows stage with introduced changes
     */
    public abstract void showStage();
}
