package ui.gui.multidrumsequencer.drumsequencer.stepbutton;

import javafx.scene.shape.Rectangle;
import sequencer.Step;

public class StepButton extends Rectangle {

    final StepButtonController stepButtonController;

    public StepButton(Step step){
        super(20, 20);
        stepButtonController = new StepButtonController(step);

        this.getStyleClass().add("stepbutton");

        stepButtonController.activeProperty.addListener((observable, oldValue, newValue) -> {
            if(newValue)
                getStyleClass().add("active");
            else getStyleClass().remove("active");
        });

        this.setOnMousePressed(stepButtonController::onPress);
    }
}
