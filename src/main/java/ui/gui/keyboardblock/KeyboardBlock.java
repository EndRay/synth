package ui.gui.keyboardblock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sequencer.Clockable;
import sequencer.MeasureDivision;
import ui.gui.KeyConsumer;
import ui.gui.draggable.Deletable;
import ui.gui.sequencer.ControlButton;
import ui.gui.keyboardblock.keyboardkey.KeyboardKey;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static ui.gui.draggable.DraggablesUtils.makeDraggable;

public class KeyboardBlock extends TitledPane implements Transmitter, Deletable, KeyConsumer, Clockable {


    final KeyboardBlockController keyboardBlockController;
    final Label label;
    List<KeyboardKey> keyboardKeys = new ArrayList<>();

    public KeyboardBlock(Receiver receiver){
        keyboardBlockController = new KeyboardBlockController(receiver);
        
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

        VBox box = new VBox();
        box.setSpacing(2);

        {
            HBox sequenceControlPanel = new HBox();
            {
                TextField stepsField = new TextField();
                stepsField.setEditable(false);
                stepsField.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
                stepsField.setAlignment(Pos.CENTER_RIGHT);
                stepsField.setPrefWidth(KeyboardKey.keyWidth * 1.2);
                stepsField.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
                stepsField.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
                stepsField.textProperty().bind(keyboardBlockController.sequenceFX.stepNumberProperty().asString());
                sequenceControlPanel.getChildren().add(stepsField);
            }
            {
                ObservableList<String> divisions = FXCollections.observableArrayList(Arrays.stream(MeasureDivision.values()).map(MeasureDivision::getShortName).toList());
                ComboBox<String> measureDivisionBox = new ComboBox<>(divisions);
                measureDivisionBox.setPrefWidth(KeyboardKey.keyWidth * 1.8);
                measureDivisionBox.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
                measureDivisionBox.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
                measureDivisionBox.getSelectionModel().select("1/4");
                measureDivisionBox.valueProperty().addListener(keyboardBlockController.divisionComboBoxListener);
                sequenceControlPanel.getChildren().add(measureDivisionBox);
            }
            {
                Button button = new ControlButton("Tie");
                button.setOnAction(keyboardBlockController::onTie);
                sequenceControlPanel.getChildren().add(button);
            }
            {
                Button button = new ControlButton("⚫");
                button.setOnAction(keyboardBlockController::onRecord);
                Consumer<Boolean> recolor = on -> button.setTextFill(on ? Color.RED : Color.DARKRED);
                keyboardBlockController.recordingProperty().addListener(
                        (observable, oldValue, newValue) -> recolor.accept(newValue));
                recolor.accept(false);
                sequenceControlPanel.getChildren().add(button);
            }
            {
                Button button = new ControlButton("M");
                button.setOnAction(keyboardBlockController::onMute);
                Consumer<Boolean> recolor = on -> button.setTextFill(on ? Color.BLUE : Color.DARKBLUE);
                keyboardBlockController.mutedProperty().addListener(
                        (observable, oldValue, newValue) -> recolor.accept(newValue));
                recolor.accept(false);
                sequenceControlPanel.getChildren().add(button);
            }


            sequenceControlPanel.setSpacing(KeyboardKey.keyWidth / 10);
            sequenceControlPanel.setPadding(new Insets(8));
            sequenceControlPanel.setAlignment(Pos.CENTER_RIGHT);

            sequenceControlPanel.setBorder(new Border(new BorderStroke(Color.GREY,
                    BorderStrokeStyle.DASHED, new CornerRadii(10), BorderWidths.DEFAULT)));

            box.getChildren().add(sequenceControlPanel);
        }

        {
            HBox playBox = new HBox(octaveDownButton, octaveUpButton, keyboardBox);
            playBox.setPadding(new Insets(8));
            playBox.setBorder(new Border(new BorderStroke(Color.GREY,
                    BorderStrokeStyle.DASHED, new CornerRadii(10), BorderWidths.DEFAULT)));

            playBox.setSpacing(KeyboardKey.keyWidth / 8);

            box.getChildren().add(playBox);
        }


        this.setContent(box);

        makeDraggable(this, label);
        keyboardBlockController.initialize();
    }

    public void pressKey(KeyboardKey key) {
        keyboardBlockController.pressKey(key);
    }

    public void releaseKey(KeyboardKey key) {
        keyboardBlockController.releaseKey(key);
    }

    public void releaseAllKeys(){
        keyboardBlockController.releaseAllKeys();
    }

    public void octaveDown(){
        keyboardBlockController.octaveDown();
    }

    public void octaveUp(){
        keyboardBlockController.octaveUp();
    }

    public void setChannel(int channel){
        keyboardBlockController.setChannel(channel);
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

            case Z -> octaveDown();
            case X -> octaveUp();
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
        keyboardBlockController.receiver = receiver;
    }

    @Override
    public Receiver getReceiver() {
        return keyboardBlockController.receiver;
    }

    @Override
    public void close() {

    }

    @Override
    public void ping() {
        keyboardBlockController.sequencer.ping();
    }

    @Override
    public void start() {
        keyboardBlockController.sequencer.start();
    }

    @Override
    public void stop() {
        keyboardBlockController.sequencer.stop();
    }

    @Override
    public void onDelete() {

    }
}
