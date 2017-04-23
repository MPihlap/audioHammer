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

    void unassigned() {
        Alert unassignedButton = new Alert(Alert.AlertType.INFORMATION);
        unassignedButton.setTitle("Unassigned!");
        unassignedButton.setHeaderText(null);
        unassignedButton.setContentText("Sorry. This button is unassigned for now. It can be used in AudioHammer's next stage.");
        unassignedButton.showAndWait();
    }

    public abstract void showStage();
}
