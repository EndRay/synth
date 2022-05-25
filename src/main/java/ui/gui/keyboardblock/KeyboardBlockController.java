package ui.gui.keyboardblock;

import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import sequencer.*;
import ui.gui.keyboardblock.keyboardkey.KeyboardKey;
import ui.gui.sequencer.SequenceFX;
import ui.gui.sequencer.SequencerPanelController;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyboardBlockController {

    public final SequencerPanelController sequencerPanelController;
    final int lowestShift = 0;
    final int highestShift = 12;
    Receiver receiver;

    int channel = -1;
    int transpose = 60;

    public KeyboardBlockController(Receiver receiver){
        this.receiver = receiver;
        sequencerPanelController = new SequencerPanelController(receiver);
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
            sequencerPanelController.addStepOnPress(new Step(new Note(key, null, null)));
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
        sequencerPanelController.setMidiChannel(channel);
    }

    @FXML
    void initialize() {
        sequencerPanelController.initialize();
    }
}
