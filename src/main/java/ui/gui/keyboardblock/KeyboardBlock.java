package ui.gui.keyboardblock;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import ui.gui.KeyConsumer;
import ui.gui.keyboardkey.KeyboardKey;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyboardBlock extends TitledPane implements Transmitter, KeyConsumer {

    Receiver receiver;

    final KeyboardBlockController keyboardBlockController;
    final Label label;
    int channel = -1;
    int transpose = 60;

    public KeyboardBlock(Receiver receiver){

        this.receiver = receiver;
        label = new Label("keyboard");
        label.minWidthProperty().bind(this.widthProperty());
        this.setGraphic(label);
        this.setMaxWidth(USE_PREF_SIZE);

        HBox topRow = new HBox();
        HBox bottomRow = new HBox();
        topRow.setMaxWidth(USE_PREF_SIZE);
        bottomRow.setMaxWidth(USE_PREF_SIZE);
        {
            topRow.getChildren().add(new Rectangle(KeyboardKey.keyWidth/2, KeyboardKey.keyHeight, Color.TRANSPARENT));
            topRow.getChildren().add(new KeyboardKey(this, 1));
            topRow.getChildren().add(new KeyboardKey(this, 3));
            topRow.getChildren().add(new Rectangle(KeyboardKey.keyWidth, KeyboardKey.keyHeight, Color.TRANSPARENT));
            topRow.getChildren().add(new KeyboardKey(this, 6));
            topRow.getChildren().add(new KeyboardKey(this, 8));
            topRow.getChildren().add(new KeyboardKey(this, 10));
            topRow.getChildren().add(new Rectangle(KeyboardKey.keyWidth, KeyboardKey.keyHeight, Color.TRANSPARENT));
            topRow.getChildren().add(new Rectangle(KeyboardKey.keyWidth/2, KeyboardKey.keyHeight, Color.TRANSPARENT));
        }
        {
            bottomRow.getChildren().add(new KeyboardKey(this, 0));
            bottomRow.getChildren().add(new KeyboardKey(this, 2));
            bottomRow.getChildren().add(new KeyboardKey(this, 4));
            bottomRow.getChildren().add(new KeyboardKey(this, 5));
            bottomRow.getChildren().add(new KeyboardKey(this, 7));
            bottomRow.getChildren().add(new KeyboardKey(this, 9));
            bottomRow.getChildren().add(new KeyboardKey(this, 11));
            bottomRow.getChildren().add(new KeyboardKey(this, 12));
        }
        topRow.setSpacing(KeyboardKey.keyWidth/8);
        bottomRow.setSpacing(KeyboardKey.keyWidth/8);

        VBox keyboardBox = new VBox(topRow, bottomRow);
        keyboardBox.setSpacing(KeyboardKey.keyWidth/8);
        keyboardBox.setMaxHeight(USE_PREF_SIZE);

        this.setContent(keyboardBox);

        keyboardBlockController = new KeyboardBlockController();
        keyboardBlockController.pane = this;
        keyboardBlockController.initialize();
        label.setOnMousePressed(keyboardBlockController::onPressed);
        label.setOnMouseDragged(keyboardBlockController::onDragged);
        label.setOnMouseReleased(keyboardBlockController::onReleased);
    }

    Set<Integer> pressedKeys = new HashSet<>();

    public void pressKey(int shift) {
        if(channel == -1)
            return;
        try {
            receiver.send(new ShortMessage(ShortMessage.NOTE_ON, channel, transpose + shift, 64), 0);
            pressedKeys.add(transpose+shift);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
    public void releaseKey(int shift) {
        if(channel == -1)
            return;
        try {
            receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, channel, transpose + shift, 0), 0);
            pressedKeys.remove(transpose+shift);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
    public void releaseAbsoluteKey(int key) {
        if(channel == -1)
            return;
        try {
            receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, channel, key, 0), 0);
            pressedKeys.remove(key);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }
    public void releaseAllKeys(){
        while (!pressedKeys.isEmpty())
            releaseAbsoluteKey(pressedKeys.stream().findAny().get());
    }

    public void setChannel(int channel){
        releaseAllKeys();
        this.channel = channel;
    }

    public void setLabelContextMenu(ContextMenu contextMenu) {
        label.setContextMenu(contextMenu);
    }

    @Override
    public void keyPressConsume(KeyCode key) {
        switch (key){
            case A -> pressKey(0);
            case W -> pressKey(1);
            case S -> pressKey(2);
            case E -> pressKey(3);
            case D -> pressKey(4);
            case F -> pressKey(5);
            case T -> pressKey(6);
            case G -> pressKey(7);
            case Y -> pressKey(8);
            case H -> pressKey(9);
            case U -> pressKey(10);
            case J -> pressKey(11);
            case K -> pressKey(12);
        }
    }

    @Override
    public void keyReleaseConsume(KeyCode key) {
        switch (key){
            case A -> releaseKey(0);
            case W -> releaseKey(1);
            case S -> releaseKey(2);
            case E -> releaseKey(3);
            case D -> releaseKey(4);
            case F -> releaseKey(5);
            case T -> releaseKey(6);
            case G -> releaseKey(7);
            case Y -> releaseKey(8);
            case H -> releaseKey(9);
            case U -> releaseKey(10);
            case J -> releaseKey(11);
            case K -> releaseKey(12);
        }
    }

    @Override
    public void unfocus() {
        releaseAllKeys();
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    @Override
    public void close() {

    }
}
