package ui.gui.keyboardblock;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import sequencer.Sequence;
import sequencer.Step;
import sequencer.*;
import ui.gui.keyboardkey.KeyboardKey;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyboardBlockController {

    Sequencer sequencer;

    public KeyboardBlockController(Receiver receiver){
        sequencer = new Sequencer(receiver, -1);
    }

    private final BooleanProperty recording = new SimpleBooleanProperty(false);
    private final BooleanProperty muted = new SimpleBooleanProperty(false);
    private Sequence newSequence = null;

    public ReadOnlyBooleanProperty recordingProperty() {
        return recording;
    }
    public boolean isRecording(){
        return recordingProperty().get();
    }
    public ReadOnlyBooleanProperty mutedProperty() {
        return muted;
    }
    public boolean isMuted(){
        return mutedProperty().get();
    }

    @FXML
    void onRecord(ActionEvent event){
        recording.setValue(recording.not().getValue());
    }

    @FXML
    void onTie(ActionEvent event){
        if(isRecording())
            newSequence.addStep(new Step());
    }

    @FXML
    void onMute(ActionEvent event){
        muted.setValue(muted.not().getValue());
    }

    @FXML
    void initialize() {
        recordingProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                newSequence = new Sequence();
            }else{
                sequencer.setSequence(newSequence);
            }
        });
        mutedProperty().addListener((observable, oldValue, newValue) -> sequencer.setMuted(newValue));
    }
}
