package ui.gui.chordmachineblock;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import midi.MidiUtils;
import sequencer.Note;
import sequencer.Step;
import ui.gui.sequencer.SequencerPanelController;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import java.util.List;

import static java.lang.Math.*;
import static midi.MidiUtils.getNoteOctave;

public class ChordKey extends StackPane {

    final public static int gridWidth = 3, gridHeight = 3;
    final public static int maxNotes = gridWidth * gridHeight;
    final public static int size = 90;

    final ChordMachineBlock chordMachineBlock;

    public ChordKey(ChordMachineBlock chordMachineBlock) {
        this.chordMachineBlock = chordMachineBlock;
        this.setPrefSize(size, size);
        this.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        this.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

        this.setAlignment(Pos.CENTER);

        Rectangle rectangle = new Rectangle(size, size);
        rectangle.setFill(Color.LIGHTGRAY);
        rectangle.setArcWidth(30);
        rectangle.setArcHeight(30);

        ObservableSet<Integer> chord = FXCollections.observableSet();
        ContextMenu menu = new ContextMenu();
        {
            int lowestOctave = getNoteOctave(MidiUtils.lowestNote),
                    highestOctave = getNoteOctave(MidiUtils.highestNote);
            for (int octave = lowestOctave; octave <= highestOctave; ++octave) {
                int lowestNoteInOctave = max((octave - lowestOctave) * 12, MidiUtils.lowestNote),
                        highestNoteInOctave = min((octave - lowestOctave) * 12 + 11, MidiUtils.highestNote);
                Menu octaveMenu = new Menu(String.valueOf(octave));
                for (int i = lowestNoteInOctave; i <= highestNoteInOctave; ++i) {
                    int note = i;
                    CheckMenuItem item = new CheckMenuItem(MidiUtils.getNoteName(note));
                    item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            if (chord.size() < maxNotes)
                                chord.add(note);
                            else item.setSelected(false);
                        } else chord.remove(note);
                    });
                    octaveMenu.getItems().add(item);
                }
                menu.getItems().add(octaveMenu);
            }
        }

        GridPane grid = new GridPane();

        grid.setVgap(3);
        grid.setHgap(3);
        grid.setAlignment(Pos.CENTER);


        chord.addListener((SetChangeListener<Integer>) change -> {
            grid.getChildren().clear();

            int notes = change.getSet().size();

            int height = (int)ceil(sqrt(notes));
            int width = (notes+height-1)/height;

            int k = 0;
            for (int note : change.getSet().stream().sorted().toList()) {
                int i = k / height,
                    j = k % height;
                Label label = new Label();
                label.setTextAlignment(TextAlignment.CENTER);
                label.setFont(Font.font("Monospaced", FontWeight.BOLD, size / 2.75 / width));
                GridPane.setHalignment(label, HPos.CENTER);
                GridPane.setValignment(label, VPos.CENTER);
                GridPane.setHgrow(label, Priority.ALWAYS);
                GridPane.setVgrow(label, Priority.ALWAYS);
                label.setText(MidiUtils.getNoteName(note));
                grid.add(label, i, j);
                ++k;
            }
        });

        this.getChildren().addAll(rectangle, grid);

        setOnContextMenuRequested(e -> menu.show(this, e.getScreenX(), e.getScreenY()));

        Step step = new Step();

        chord.addListener((SetChangeListener<? super Integer>) e -> {
            step.clearNotes();
            for(int note : e.getSet())
                step.addNote(new Note(note, null, null));
        });

        setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                chordMachineBlock.chordMachineBlockController.sequencerPanelController.addStepOnPress(step);
                int channel = chordMachineBlock.getChannel();
                if (channel == -1)
                    return;
                try {
                    for (int note : chord)
                        chordMachineBlock.getReceiver().send(new ShortMessage(ShortMessage.NOTE_ON, channel, note, 64), 0);
                } catch (InvalidMidiDataException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                int channel = chordMachineBlock.getChannel();
                if (channel == -1)
                    return;
                try {
                    for (int note : chord)
                        chordMachineBlock.getReceiver().send(new ShortMessage(ShortMessage.NOTE_OFF, channel, note, 0), 0);
                } catch (InvalidMidiDataException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
