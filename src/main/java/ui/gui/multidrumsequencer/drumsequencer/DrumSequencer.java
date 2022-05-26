package ui.gui.multidrumsequencer.drumsequencer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import midi.MidiUtils;
import sequencer.Clockable;
import ui.gui.sequencer.ControlButton;

import javax.sound.midi.Receiver;
import java.util.function.Consumer;

public class DrumSequencer extends HBox implements Clockable {

    DrumSequencerController drumSequencerController;

    public DrumSequencer(Receiver receiver, int channel) {
        drumSequencerController = new DrumSequencerController(receiver, channel);

        this.getStyleClass().addAll("control-block", "control-panel", "left");
        {
            {
                Button button = new ControlButton("M");
                button.setOnAction(drumSequencerController::onMute);
                Consumer<Boolean> recolor = on -> button.setTextFill(on ? Color.BLUE : Color.DARKBLUE);
                drumSequencerController.mutedProperty().addListener(
                        (observable, oldValue, newValue) -> recolor.accept(newValue));
                recolor.accept(false);
                this.getChildren().add(button);
            }
            {
                ObservableList<String> notes = FXCollections.observableArrayList();
                for(int i = MidiUtils.lowestNote; i <= MidiUtils.highestNote; ++i)
                    notes.add(MidiUtils.getNoteName(i));
                ComboBox<String> notesBox = new ComboBox<>(notes);
                notesBox.setPrefWidth(80);
                notesBox.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
                notesBox.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
                notesBox.getSelectionModel().select("C3");
                notesBox.valueProperty().addListener((observable, oldValue, newValue) -> drumSequencerController.setNote(MidiUtils.getNoteByName(newValue)));
                this.getChildren().add(notesBox);
            }
            {
                HBox stepsPane = new HBox();
                stepsPane.setAlignment(Pos.CENTER);
                stepsPane.setSpacing(1);
                drumSequencerController.stepsPane = stepsPane;
                this.getChildren().add(stepsPane);
            }
            {
                Button button = new ControlButton("+");
                button.setOnAction(e -> drumSequencerController.addStep());
                drumSequencerController.addStepButton = button;
                this.getChildren().add(button);
            }
            {
                Button button = new ControlButton("-");
                button.setOnAction(e -> drumSequencerController.removeStep());
                drumSequencerController.removeStepButton = button;
                this.getChildren().add(button);
            }
        }
        drumSequencerController.initialize();
    }

    public void setChannel(int channel){
        drumSequencerController.sequencer.setMidiChannel(channel);
    }

    @Override
    public void ping() {
        drumSequencerController.sequencer.ping();
    }

    @Override
    public void start() {
        drumSequencerController.sequencer.start();
    }

    @Override
    public void stop() {
        drumSequencerController.sequencer.stop();
    }
}
