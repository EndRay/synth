package ui.gui.multidrumsequencer.drumsequencer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import sequencer.Sequence;
import sequencer.Sequencer;
import sequencer.Step;
import ui.gui.multidrumsequencer.drumsequencer.stepbutton.StepButton;

import javax.sound.midi.Receiver;
import java.util.ArrayList;
import java.util.List;

public class DrumSequencerController {
    Sequencer sequencer;
    Sequence sequence;

    BooleanProperty mutedProperty = new SimpleBooleanProperty();

    Pane stepsPane;
    Button addStepButton;
    Button removeStepButton;

    DrumSequencerController(Receiver receiver, int channel){
        sequencer = new Sequencer(receiver, channel);
        sequence = new Sequence();
        sequencer.setSequence(sequence);
    }

    @FXML
    void initialize(){
        for(int i = 0; i < 16; ++i)
            addStep();
    }

    void onMute(ActionEvent e){
        mutedProperty.set(mutedProperty.not().get());
        sequencer.setMuted(mutedProperty.get());
    }

    ReadOnlyBooleanProperty mutedProperty(){
        return mutedProperty;
    }

    public void addStep(){
        Step step = new Step();
        sequence.addStep(step);
        stepsPane.getChildren().add(new StepButton(step));
    }

    public void removeStep(){
        if(sequence.length() == 0)
            return;
        sequence.removeStep();
        stepsPane.getChildren().remove(stepsPane.getChildren().size()-1);
    }

    public void setNote(int note){
        sequence.setDefaultPitch(note);
    }
}
