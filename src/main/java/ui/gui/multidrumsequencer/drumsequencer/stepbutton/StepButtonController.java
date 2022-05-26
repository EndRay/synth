package ui.gui.multidrumsequencer.drumsequencer.stepbutton;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.MouseEvent;
import sequencer.Note;
import sequencer.Step;

public class StepButtonController {
    final Step step;

    final BooleanProperty activeProperty = new SimpleBooleanProperty();

    int note;

    StepButtonController(Step step){
        this.step = step;
    }

    ReadOnlyBooleanProperty activeProperty(){
        return activeProperty;
    }

    void onPress(MouseEvent e){
        activeProperty.set(activeProperty.not().get());
        if(activeProperty.get()){
            step.addNote(new Note(null, null, null));
        } else step.clearNotes();
    }
}
