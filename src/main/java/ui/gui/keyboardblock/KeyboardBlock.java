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
import javafx.scene.text.FontWeight;
import ui.gui.KeyConsumer;
import ui.gui.keyboardkey.KeyboardKey;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.*;

public class KeyboardBlock extends TitledPane implements Transmitter, KeyConsumer {

    final int lowestShift = 0;
    final int highestShift = 12;

    Receiver receiver;

    final KeyboardBlockController keyboardBlockController;
    final Label label;
    int channel = -1;
    int transpose = 60;
    List<KeyboardKey> keyboardKeys = new ArrayList<>();

    public KeyboardBlock(Receiver receiver){

        this.receiver = receiver;
        label = new Label("keyboard");
        label.minWidthProperty().bind(this.widthProperty());
        this.setGraphic(label);
        this.setMaxWidth(USE_PREF_SIZE);

        HBox topRow = new HBox();
        HBox bottomRow = new HBox();
        for(int i = 0; i < 13; ++i)
            keyboardKeys.add(new KeyboardKey(this, i));
        topRow.setMaxWidth(USE_PREF_SIZE);
        bottomRow.setMaxWidth(USE_PREF_SIZE);
        {
            topRow.getChildren().add(new Rectangle(KeyboardKey.keyWidth/2, KeyboardKey.keyHeight, Color.TRANSPARENT));
            topRow.getChildren().add(keyboardKeys.get(1));
            topRow.getChildren().add(keyboardKeys.get(3));
            topRow.getChildren().add(new Rectangle(KeyboardKey.keyWidth, KeyboardKey.keyHeight, Color.TRANSPARENT));
            topRow.getChildren().add(keyboardKeys.get(6));
            topRow.getChildren().add(keyboardKeys.get(8));
            topRow.getChildren().add(keyboardKeys.get(10));
            topRow.getChildren().add(new Rectangle(KeyboardKey.keyWidth, KeyboardKey.keyHeight, Color.TRANSPARENT));
            topRow.getChildren().add(new Rectangle(KeyboardKey.keyWidth/2, KeyboardKey.keyHeight, Color.TRANSPARENT));
        }
        {
            bottomRow.getChildren().add(keyboardKeys.get(0));
            bottomRow.getChildren().add(keyboardKeys.get(2));
            bottomRow.getChildren().add(keyboardKeys.get(4));
            bottomRow.getChildren().add(keyboardKeys.get(5));
            bottomRow.getChildren().add(keyboardKeys.get(7));
            bottomRow.getChildren().add(keyboardKeys.get(9));
            bottomRow.getChildren().add(keyboardKeys.get(11));
            bottomRow.getChildren().add(keyboardKeys.get(12));
        }
        topRow.setSpacing(KeyboardKey.keyWidth/8);
        bottomRow.setSpacing(KeyboardKey.keyWidth/8);

        VBox keyboardBox = new VBox(topRow, bottomRow);
        keyboardBox.setSpacing(KeyboardKey.keyWidth/8);
        keyboardBox.setMaxHeight(USE_PREF_SIZE);

        Button octaveDownButton = new Button("<");
        octaveDownButton.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        octaveDownButton.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        octaveDownButton.setPrefSize(KeyboardKey.keyWidth/2, KeyboardKey.keyWidth/2);
        octaveDownButton.setFont(Font.font("Monospaced", FontWeight.BOLD, KeyboardKey.keyWidth/5));
        octaveDownButton.setOnAction(event -> octaveDown());

        Button octaveUpButton = new Button(">");
        octaveUpButton.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        octaveUpButton.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        octaveUpButton.setPrefSize(KeyboardKey.keyWidth/2, KeyboardKey.keyWidth/2);
        octaveUpButton.setFont(Font.font("Monospaced", FontWeight.BOLD, KeyboardKey.keyWidth/5));
        octaveUpButton.setOnAction(event -> octaveUp());

        HBox box = new HBox(octaveDownButton, octaveUpButton, keyboardBox);

        box.setSpacing(KeyboardKey.keyWidth/8);

        this.setContent(box);

        keyboardBlockController = new KeyboardBlockController();
        keyboardBlockController.pane = this;
        keyboardBlockController.initialize();
        label.setOnMousePressed(keyboardBlockController::onPressed);
        label.setOnMouseDragged(keyboardBlockController::onDragged);
        label.setOnMouseReleased(keyboardBlockController::onReleased);
    }

    Set<Integer> pressedKeys = new HashSet<>();
    Map<KeyboardKey, Integer> pressedKeyByKeyboardKey = new HashMap<>();

    public void pressKey(KeyboardKey key) {
        if(channel == -1)
            return;
        releaseKey(key);
        try {
            int shift = key.shift;
            receiver.send(new ShortMessage(ShortMessage.NOTE_ON, channel, transpose + shift, 64), 0);
            pressedKeys.add(transpose+shift);
            pressedKeyByKeyboardKey.put(key, transpose+shift);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void releaseKey(KeyboardKey key) {
        if(channel == -1)
            return;
        if(pressedKeyByKeyboardKey.containsKey(key))
            releaseAbsoluteKey(pressedKeyByKeyboardKey.get(key));
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
    }

    public void setLabelContextMenu(ContextMenu contextMenu) {
        label.setContextMenu(contextMenu);
    }

    @Override
    public void keyPressConsume(KeyCode key) {
        switch (key){
            case A -> pressKey(keyboardKeys.get(0));
            case W -> pressKey(keyboardKeys.get(1));
            case S -> pressKey(keyboardKeys.get(2));
            case E -> pressKey(keyboardKeys.get(3));
            case D -> pressKey(keyboardKeys.get(4));
            case F -> pressKey(keyboardKeys.get(5));
            case T -> pressKey(keyboardKeys.get(6));
            case G -> pressKey(keyboardKeys.get(7));
            case Y -> pressKey(keyboardKeys.get(8));
            case H -> pressKey(keyboardKeys.get(9));
            case U -> pressKey(keyboardKeys.get(10));
            case J -> pressKey(keyboardKeys.get(11));
            case K -> pressKey(keyboardKeys.get(12));
        }
    }

    @Override
    public void keyReleaseConsume(KeyCode key) {
        switch (key){
            case A -> releaseKey(keyboardKeys.get(0));
            case W -> releaseKey(keyboardKeys.get(1));
            case S -> releaseKey(keyboardKeys.get(2));
            case E -> releaseKey(keyboardKeys.get(3));
            case D -> releaseKey(keyboardKeys.get(4));
            case F -> releaseKey(keyboardKeys.get(5));
            case T -> releaseKey(keyboardKeys.get(6));
            case G -> releaseKey(keyboardKeys.get(7));
            case Y -> releaseKey(keyboardKeys.get(8));
            case H -> releaseKey(keyboardKeys.get(9));
            case U -> releaseKey(keyboardKeys.get(10));
            case J -> releaseKey(keyboardKeys.get(11));
            case K -> releaseKey(keyboardKeys.get(12));
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
