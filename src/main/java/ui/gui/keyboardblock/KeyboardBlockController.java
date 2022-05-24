package ui.gui.keyboardblock;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import sequencer.*;
import ui.gui.keyboardkey.KeyboardKey;
import ui.gui.sequencer.SequenceFX;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyboardBlockController {

    final int lowestShift = 0;
    final int highestShift = 12;
    Receiver receiver;

    int channel = -1;
    int transpose = 60;
    final Sequencer sequencer;
    final SequenceFX sequenceFX;

    public KeyboardBlockController(Receiver receiver){
        this.receiver = receiver;
        sequencer = new Sequencer(receiver, -1);
        sequenceFX = new SequenceFX();
    }

    private final BooleanProperty recording = new SimpleBooleanProperty(false);
    private final BooleanProperty muted = new SimpleBooleanProperty(false);
    private Sequence newSequence = null;
    private MeasureDivision nowMeasureDivision = MeasureDivision.QUARTER;

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

    ChangeListener<String> divisionComboBoxListener = (observableValue, oldValue, newValue) -> {
        for(MeasureDivision our : MeasureDivision.values())
            if(our.getShortName().equals(newValue))
                nowMeasureDivision = our;
        if(newSequence != null)
            newSequence.setMeasureDivision(nowMeasureDivision);
    };

    @FXML
    void onTie(ActionEvent event){
        if(isRecording()) {
            newSequence.addStep(new Step());
            sequenceFX.updateProperties();
        }
    }

    @FXML
    void onMute(ActionEvent event){
        muted.setValue(muted.not().getValue());
    }

    Set<Integer> pressedKeys = new HashSet<>();
    Map<KeyboardKey, Integer> pressedKeyByKeyboardKey = new HashMap<>();

    public void pressKey(KeyboardKey key) {
        releaseKey(key);
        int keyNote = transpose+key.shift;
        pressAbsoluteKey(keyNote);
        pressedKeyByKeyboardKey.put(key, keyNote);
    }

    public void pressAbsoluteKey(int key){
        try {
            if(isRecording()){
                newSequence.addStep(new Step(new Note(key, 64, 0.5)));
                sequenceFX.updateProperties();
            }
            if(channel == -1)
                return;
            receiver.send(new ShortMessage(ShortMessage.NOTE_ON, channel, key, 64), 0);
            pressedKeys.add(key);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void releaseKey(KeyboardKey key) {
        if(pressedKeyByKeyboardKey.containsKey(key)) {
            releaseAbsoluteKey(pressedKeyByKeyboardKey.get(key));
            pressedKeyByKeyboardKey.remove(key);
        }
    }

    public void releaseAbsoluteKey(int key) {
        try {
            if(channel == -1)
                return;
            receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, channel, key, 0), 0);
            pressedKeys.remove(key);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
    public void releaseAllKeys(){
        while (!pressedKeys.isEmpty())
            releaseAbsoluteKey(pressedKeys.stream().findAny().get());
        pressedKeyByKeyboardKey.clear();
    }

    public void octaveDown(){
        if(transpose - 12 + lowestShift >= 0)
            transpose -= 12;
    }

    public void octaveUp(){
        if(transpose + 12 + highestShift <= 127)
            transpose += 12;
    }

    public void setChannel(int channel){
        releaseAllKeys();
        this.channel = channel;
        sequencer.setMidiChannel(channel);
    }

    @FXML
    void initialize() {
        recordingProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                newSequence = new Sequence(nowMeasureDivision);
                sequenceFX.setSequence(newSequence);
                sequenceFX.updateProperties();
            }else{
                sequencer.setSequence(newSequence);
            }
        });
        mutedProperty().addListener((observable, oldValue, newValue) -> sequencer.setMuted(newValue));
    }
}
