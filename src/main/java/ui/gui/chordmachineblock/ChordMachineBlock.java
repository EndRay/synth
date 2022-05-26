package ui.gui.chordmachineblock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import midi.MidiUtils;
import sequencer.Clockable;
import sequencer.MeasureDivision;
import ui.gui.draggable.Deletable;
import ui.gui.keyboardblock.keyboardkey.KeyboardKey;
import ui.gui.sequencer.ControlButton;
import ui.gui.sequencer.SequencerPanelController;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import java.util.Arrays;
import java.util.function.Consumer;

import static ui.gui.draggable.DraggablesUtils.makeDraggable;

public class ChordMachineBlock extends TitledPane implements Transmitter, Deletable, Clockable {
    final Label label;
    final ChordMachineBlockController chordMachineBlockController;

    final public static int width = 4;
    final public static int height = 2;

    Receiver receiver;

    int channel;

    public ChordMachineBlock(Receiver receiver){
        this.receiver = receiver;
        chordMachineBlockController = new ChordMachineBlockController(receiver);
        label = new Label("chord machine");
        label.minWidthProperty().bind(this.widthProperty());
        this.setGraphic(label);
        this.setMaxWidth(USE_PREF_SIZE);
        makeDraggable(this, label);
        VBox ChordMachineBox = new VBox();
        this.setContent(ChordMachineBox);
        {
            HBox sequenceControlPanel = new HBox();
            sequenceControlPanel.getStyleClass().addAll("control-block", "control-panel");
            {
                TextField stepsField = new TextField();
                stepsField.setEditable(false);
                stepsField.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
                stepsField.setAlignment(Pos.CENTER_RIGHT);
                stepsField.setPrefWidth(KeyboardKey.keyWidth * 1.2);
                stepsField.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
                stepsField.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
                stepsField.textProperty().bind(chordMachineBlockController.sequencerPanelController.sequenceFX.stepNumberProperty().asString());
                sequenceControlPanel.getChildren().add(stepsField);
            }
            {
                ObservableList<String> divisions = FXCollections.observableArrayList(Arrays.stream(MeasureDivision.values()).map(MeasureDivision::getShortName).toList());
                ComboBox<String> measureDivisionBox = new ComboBox<>(divisions);
                measureDivisionBox.setPrefWidth(KeyboardKey.keyWidth * 2.2);
                measureDivisionBox.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
                measureDivisionBox.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
                measureDivisionBox.getSelectionModel().select("1/4");
                measureDivisionBox.valueProperty().addListener(chordMachineBlockController.sequencerPanelController.divisionComboBoxListener);
                sequenceControlPanel.getChildren().add(measureDivisionBox);
            }
            {
                Spinner<Double> GateSpinner = new Spinner<>();
                GateSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 0.9, SequencerPanelController.defaultDefaultGate, 0.1));
                GateSpinner.setViewOrder(0.1);
                GateSpinner.setPrefWidth(ControlButton.buttonSize * 2.5);
                GateSpinner.setMinWidth(USE_PREF_SIZE);
                GateSpinner.setMaxWidth(USE_PREF_SIZE);
                GateSpinner.setEditable(true);
                GateSpinner.valueProperty().addListener(chordMachineBlockController.sequencerPanelController.gateSpinnerListener);
                sequenceControlPanel.getChildren().add(GateSpinner);
            }
            {
                Button button = new ControlButton("Tie");
                button.setOnAction(chordMachineBlockController.sequencerPanelController::onTie);
                sequenceControlPanel.getChildren().add(button);
            }
            {
                Button button = new ControlButton("⚫");
                button.setOnAction(chordMachineBlockController.sequencerPanelController::onRecord);
                Consumer<Boolean> recolor = on -> button.setTextFill(on ? Color.RED : Color.DARKRED);
                chordMachineBlockController.sequencerPanelController.recordingProperty().addListener(
                        (observable, oldValue, newValue) -> recolor.accept(newValue));
                recolor.accept(false);
                sequenceControlPanel.getChildren().add(button);
            }
            {
                Button button = new ControlButton("M");
                button.setOnAction(chordMachineBlockController.sequencerPanelController::onMute);
                Consumer<Boolean> recolor = on -> button.setTextFill(on ? Color.BLUE : Color.DARKBLUE);
                chordMachineBlockController.sequencerPanelController.mutedProperty().addListener(
                        (observable, oldValue, newValue) -> recolor.accept(newValue));
                recolor.accept(false);
                sequenceControlPanel.getChildren().add(button);
            }


            ChordMachineBox.getChildren().add(sequenceControlPanel);
        }
        {

            {
                GridPane grid = new GridPane();
                for (int i = 0; i < width; ++i)
                    for (int j = 0; j < height; ++j)
                        grid.add(new ChordKey(this), i, j);
                grid.setHgap(10);
                grid.setVgap(10);

                grid.getStyleClass().add("control-block");

                ChordMachineBox.getChildren().add(grid);
            }

            ToggleGroup group = new ToggleGroup();

            ContextMenu menu = new ContextMenu();
            for (int i = 0; i < MidiUtils.channels; ++i) {
                int channel = i;
                RadioMenuItem item = new RadioMenuItem("midi channel " + (channel + 1));
                item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue)
                        setChannel(channel);
                });
                item.setToggleGroup(group);
                menu.getItems().add(item);
            }
            {
                RadioMenuItem item = new RadioMenuItem("disabled");
                item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue)
                        setChannel(-1);
                });
                menu.getItems().add(item);
                item.setToggleGroup(group);
                item.setSelected(true);
            }
            label.setContextMenu(menu);
        }
    }

    public void setChannel(int channel){
        this.channel = channel;
        chordMachineBlockController.sequencerPanelController.sequencer.setMidiChannel(channel);

    }

    public int getChannel(){
        return channel;
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

    @Override
    public void onDelete() {

    }

    @Override
    public void ping() {
        chordMachineBlockController.sequencerPanelController.sequencer.ping();
    }

    @Override
    public void start() {
        chordMachineBlockController.sequencerPanelController.sequencer.start();
    }

    @Override
    public void stop() {
        chordMachineBlockController.sequencerPanelController.sequencer.stop();
    }
}
