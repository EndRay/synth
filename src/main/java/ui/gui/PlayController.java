package ui.gui;

import database.NoSuchSynthException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import midi.MidiUtils;
import sequencer.Clock;
import structscript.StructScriptException;
import structscript.polyphony.PolyphonyException;
import structscript.polyphony.PolyphonyType;
import structscript.polyphony.PolyphonyUtils;
import synthesizer.sources.utils.Socket;
import ui.gui.chordmachineblock.ChordMachineBlock;
import ui.gui.keyboardblock.KeyboardBlock;
import ui.gui.multidrumsequencer.DrumSequencerBlock;
import ui.gui.sequencer.ControlButton;
import ui.gui.synthblock.SynthBlock;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import static database.Database.getSynths;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import static midi.MidiUtils.getNoteOctave;
import static ui.gui.MainGUI.*;
import static ui.gui.volume.VolumeUtils.makeVolumeSlider;


public class PlayController {

    Socket playgroundSound = new Socket();

    @FXML
    Pane table;
    @FXML
    ComboBox<String> synthNameField;
    @FXML
    TextField voiceCountField;

    @FXML
    TextField messageText;

    @FXML
    HBox clockControls;

    @FXML
    Slider masterVolumeSlider;

    Clock clock = new Clock();
    BooleanProperty playingProperty = new SimpleBooleanProperty(false);

    private int lastViewOrder = 0;
    private void reorderOnFocus(Node node){
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!oldValue && newValue)
                node.setViewOrder(--lastViewOrder);
        });
    }

    private void clockBPMset(double BPM){
        clock.setBPM(BPM);
    }

    @FXML
    void goToMainMenu(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            Parent root = new FXMLLoader(EditController.class.getResource("main-menu.fxml")).load();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void createSynthBlock() {
        String synthName = synthNameField.getValue();
        if(synthName == null){
            messageText.setText("choose synth to load first");
            return;
        }
        synthName = synthName.trim();
        String synth = synthName;
        try {
            PolyphonyType polyphony = PolyphonyUtils.byString(voiceCountField.getCharacters().toString().trim());
            SynthBlock synthBlock = new SynthBlock(synth, polyphony);
            table.getChildren().add(synthBlock);
            playgroundSound.modulate(synthBlock.getSound());

            ContextMenu menu = new ContextMenu();
            for (int i = 0; i < MidiUtils.channels; ++i) {
                int channel = i;
                CheckMenuItem item = new CheckMenuItem("midi channel " + (channel + 1));
                item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue)
                        receiver.addSynthController(channel, synthBlock.getSynthController());
                    else receiver.removeSynthController(channel, synthBlock.getSynthController());
                });
                menu.getItems().add(item);
            }
            IntegerProperty splitFrom = new SimpleIntegerProperty(0);
            IntegerProperty splitTo = new SimpleIntegerProperty(127);
            Menu splitFromMenu = new Menu("lowest");
            {
                ToggleGroup group = new ToggleGroup();
                int lowestOctave = getNoteOctave(MidiUtils.lowestNote),
                        highestOctave = getNoteOctave(MidiUtils.highestNote);
                for (int octave = lowestOctave; octave <= highestOctave; ++octave) {
                    int lowestNoteInOctave = max((octave - lowestOctave) * 12, MidiUtils.lowestNote),
                            highestNoteInOctave = min((octave - lowestOctave) * 12 + 11, MidiUtils.highestNote);
                    Menu octaveMenu = new Menu(String.valueOf(octave));
                    for (int i = lowestNoteInOctave; i <= highestNoteInOctave; ++i) {
                        int note = i;
                        RadioMenuItem item = new RadioMenuItem(MidiUtils.getNoteName(note));
                        item.setToggleGroup(group);
                        item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue)
                                splitFrom.setValue(note);
                        });
                        splitTo.addListener((observable, oldValue, newValue) -> item.setDisable(note > newValue.intValue()));
                        octaveMenu.getItems().add(item);
                    }
                    splitTo.addListener((observable, oldValue, newValue) -> octaveMenu.setDisable(lowestNoteInOctave > newValue.intValue()));
                    splitFromMenu.getItems().add(octaveMenu);
                }
            }
            Menu splitToMenu = new Menu("highest");
            {
                ToggleGroup group = new ToggleGroup();
                int lowestOctave = getNoteOctave(MidiUtils.lowestNote),
                        highestOctave = getNoteOctave(MidiUtils.highestNote);
                for (int octave = lowestOctave; octave <= highestOctave; ++octave) {
                    int lowestNoteInOctave = max((octave - lowestOctave) * 12, MidiUtils.lowestNote),
                            highestNoteInOctave = min((octave - lowestOctave) * 12 + 11, MidiUtils.highestNote);
                    Menu octaveMenu = new Menu(String.valueOf(octave));
                    for (int i = lowestNoteInOctave; i <= highestNoteInOctave; ++i) {
                        int note = i;
                        RadioMenuItem item = new RadioMenuItem(MidiUtils.getNoteName(note));
                        item.setToggleGroup(group);
                        item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue)
                                splitTo.setValue(note);
                        });
                        splitFrom.addListener((observable, oldValue, newValue) -> item.setDisable(note < newValue.intValue()));
                        octaveMenu.getItems().add(item);
                    }
                    splitFrom.addListener((observable, oldValue, newValue) -> octaveMenu.setDisable(highestNoteInOctave < newValue.intValue()));
                    splitToMenu.getItems().add(octaveMenu);
                }
            }
            Runnable updateCondition = () -> {
                int from = splitFrom.intValue(),
                        to = splitTo.intValue();
                synthBlock.getSynthController().setCondition(note -> from <= note && note <= to);
            };
            splitFrom.addListener((observable, oldValue, newValue) -> updateCondition.run());
            splitTo.addListener((observable, oldValue, newValue) -> updateCondition.run());
            menu.getItems().addAll(new SeparatorMenuItem(), splitFromMenu, splitToMenu);
            synthBlock.setLabelContextMenu(menu);

            reorderOnFocus(synthBlock);

            messageText.setText("synth successfully created");
        } catch (NoSuchSynthException e) {
            messageText.setText("no such synth \"" + synth + "\"");
        } catch (StructScriptException e) {
            messageText.setText(e.getStructScriptMessage());
        } catch (PolyphonyException e) {
            messageText.setText("incorrect polyphony type");
        }
    }

    Set<KeyCode> pressedKeys = new HashSet<>();
    KeyConsumer focusedConsumer;
    ReadWriteLock focusedConsumerLock = new ReentrantReadWriteLock();

    void setFocusedConsumer(KeyConsumer consumer) {
        if (focusedConsumer == consumer)
            return;
        try {
            focusedConsumerLock.writeLock().lock();
            if (focusedConsumer != null)
                focusedConsumer.unfocus();
            focusedConsumer = consumer;
        } finally {
            focusedConsumerLock.writeLock().unlock();
        }
    }

    KeyConsumer getFocusedConsumer() {
        try {
            focusedConsumerLock.readLock().lock();
            return focusedConsumer;
        } finally {
            focusedConsumerLock.readLock().unlock();
        }
    }

    void configureSceneKeyConsuming() {
        Scene scene = table.getScene();
        scene.focusOwnerProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof KeyConsumer consumer)
                setFocusedConsumer(consumer);
        });
        scene.setOnKeyPressed(event -> {
            if (pressedKeys.contains(event.getCode()))
                return;
            pressedKeys.add(event.getCode());
            KeyConsumer consumer = getFocusedConsumer();
            if (consumer != null)
                consumer.keyPressConsume(event.getCode());
        });
        scene.setOnKeyReleased(event -> {
            if (!pressedKeys.contains(event.getCode()))
                return;
            pressedKeys.remove(event.getCode());
            KeyConsumer consumer = getFocusedConsumer();
            if (consumer != null)
                consumer.keyReleaseConsume(event.getCode());
        });
    }

    @FXML
    void createKeyboardBlock() {
        configureSceneKeyConsuming();

        KeyboardBlock keyboardBlock = new KeyboardBlock(receiver);

        clock.add(keyboardBlock);

        table.getChildren().add(keyboardBlock);

        ToggleGroup group = new ToggleGroup();

        ContextMenu menu = new ContextMenu();
        for (int i = 0; i < MidiUtils.channels; ++i) {
            int channel = i;
            RadioMenuItem item = new RadioMenuItem("midi channel " + (channel + 1));
            item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue)
                    keyboardBlock.setChannel(channel);
            });
            item.setToggleGroup(group);
            menu.getItems().add(item);
        }
        {
            RadioMenuItem item = new RadioMenuItem("disabled");
            item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue)
                    keyboardBlock.setChannel(-1);
            });
            menu.getItems().add(item);
            item.setToggleGroup(group);
            item.setSelected(true);
        }
        keyboardBlock.setLabelContextMenu(menu);

        reorderOnFocus(keyboardBlock);

        messageText.setText("keyboard successfully created");
    }

    @FXML
    void createChordMachineBlock() {
        configureSceneKeyConsuming();

        ChordMachineBlock chordMachine = new ChordMachineBlock(receiver);
        clock.add(chordMachine);
        table.getChildren().add(chordMachine);

        reorderOnFocus(chordMachine);
    }

    @FXML
    void createDrumSequencerBlock(){
        DrumSequencerBlock drumSequencer = new DrumSequencerBlock(receiver);
        clock.add(drumSequencer);
        table.getChildren().add(drumSequencer);

        reorderOnFocus(drumSequencer);
    }

    @FXML
    void initialize() {
        sound.bind(playgroundSound);
        masterVolumeSlider.setValue(masterVolume.getValue());
        makeVolumeSlider(masterVolumeSlider, masterVolume);

        {
            synthNameField.setItems(FXCollections.observableList(getSynths()));
        }

        {
            Spinner<Integer> BPMspinner = new Spinner<>();
            BPMspinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,  999));
            BPMspinner.getValueFactory().setValue(120);
            BPMspinner.setPrefWidth(ControlButton.buttonSize * 3);
            BPMspinner.setMinWidth(USE_PREF_SIZE);
            BPMspinner.setMaxWidth(USE_PREF_SIZE);
            BPMspinner.setEditable(true);
            BPMspinner.valueProperty().addListener((observable, oldValue, newValue) -> clockBPMset(newValue));
            clockControls.getChildren().add(BPMspinner);
        }
        {
            Button button = new ControlButton("▶");
            button.setOnAction(event -> {
                clock.start();
                playingProperty.set(true);
            });
            Consumer<Boolean> recolor = on -> button.setTextFill(on ? Color.LIMEGREEN : Color.DARKGREEN);
            playingProperty.addListener((observable, oldValue, newValue) -> recolor.accept(newValue));
            recolor.accept(false);
            clockControls.getChildren().add(button);
        }
        {
            Button button = new ControlButton("⯀");
            button.setOnAction(event -> {
                clock.stop();
                playingProperty.set(false);
            });
            clockControls.getChildren().add(button);
        }
    }
}
