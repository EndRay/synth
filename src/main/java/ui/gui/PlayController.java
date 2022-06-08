package ui.gui;

import database.Database;
import database.NoSuchSetupException;
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
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.Socket;
import ui.gui.chordmachineblock.ChordMachineBlock;
import ui.gui.draggable.Deletable;
import ui.gui.keyboardblock.KeyboardBlock;
import ui.gui.multidrumsequencer.DrumSequencerBlock;
import ui.gui.sequencer.ControlButton;
import ui.gui.synthblock.SynthBlock;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import static database.Database.getSetup;
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
    TextField setupNameField;

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

    private void reorderOnFocus(Node node) {
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue && newValue)
                node.setViewOrder(--lastViewOrder);
        });
    }

    private void clockBPMset(double BPM) {
        clock.setBPM(BPM);
    }

    @FXML
    void goToMainMenu(ActionEvent event) {
        saveSetup("");
        clock.clear();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        try {
            Parent root = new FXMLLoader(EditController.class.getResource("main-menu.fxml")).load();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void createSynthBlock(String synth, PolyphonyType polyphony) throws NoSuchSynthException, StructScriptException {
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
                    splitFrom.addListener((observable, oldValue, newValue) -> item.setSelected(note == newValue.intValue()));
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
                    splitTo.addListener((observable, oldValue, newValue) -> item.setSelected(note == newValue.intValue()));
                    octaveMenu.getItems().add(item);
                }
                splitFrom.addListener((observable, oldValue, newValue) -> octaveMenu.setDisable(highestNoteInOctave < newValue.intValue()));
                splitToMenu.getItems().add(octaveMenu);
            }
        }
        Menu splitOnlyMenu = new Menu("only");
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
                        if (newValue) {
                            splitFrom.setValue(note);
                            splitTo.setValue(note);
                        }
                    });
                    splitFrom.addListener((observable, oldValue, newValue) ->
                            item.setSelected(note == splitFrom.intValue() && note == splitTo.intValue()));
                    splitTo.addListener((observable, oldValue, newValue) ->
                            item.setSelected(note == splitFrom.intValue() && note == splitTo.intValue()));
                    octaveMenu.getItems().add(item);
                }
                splitOnlyMenu.getItems().add(octaveMenu);
            }
        }
        Runnable updateCondition = () -> {
            int from = splitFrom.intValue(),
                    to = splitTo.intValue();
            synthBlock.getSynthController().setCondition(note -> from <= note && note <= to);
        };
        splitFrom.addListener((observable, oldValue, newValue) -> updateCondition.run());
        splitTo.addListener((observable, oldValue, newValue) -> updateCondition.run());
        menu.getItems().addAll(new SeparatorMenuItem(), splitFromMenu, splitToMenu, splitOnlyMenu);
        synthBlock.setLabelContextMenu(menu);

        reorderOnFocus(synthBlock);

    }

    @FXML
    void createSynthBlock() {
        String synthName = synthNameField.getValue();
        if (synthName == null) {
            messageText.setText("choose synth to load first");
            return;
        }
        synthName = synthName.trim();
        String synth = synthName;
        try {
            PolyphonyType polyphony = PolyphonyUtils.byString(voiceCountField.getCharacters().toString().trim());
            createSynthBlock(synth, polyphony);
            messageText.setText("synth successfully created");
        } catch (PolyphonyException e) {
            messageText.setText("incorrect polyphony type");
        } catch (NoSuchSynthException e) {
            messageText.setText("no such synth \"" + synth + "\"");
        } catch (StructScriptException e) {
            messageText.setText(e.getStructScriptMessage());
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

        reorderOnFocus(keyboardBlock);

        messageText.setText("keyboard successfully created");
    }

    void fillWithBasicChords(ChordMachineBlock chordMachine) {
        chordMachine.setChord(0, 0, MidiUtils.getQuickChord("F3"));
        chordMachine.setChord(0, 1, MidiUtils.getQuickChord("D3m"));
        chordMachine.setChord(1, 0, MidiUtils.getQuickChord("C3"));
        chordMachine.setChord(1, 1, MidiUtils.getQuickChord("A3m"));
        chordMachine.setChord(2, 0, MidiUtils.getQuickChord("G3"));
        chordMachine.setChord(2, 1, MidiUtils.getQuickChord("E3m"));

        chordMachine.setChord(3, 1, MidiUtils.getQuickChord("E3"));
    }

    @FXML
    void createChordMachineBlock() {
        configureSceneKeyConsuming();

        ChordMachineBlock chordMachine = new ChordMachineBlock(receiver);
        clock.add(chordMachine);
        table.getChildren().add(chordMachine);

        fillWithBasicChords(chordMachine);

        reorderOnFocus(chordMachine);
    }

    @FXML
    void createDrumSequencerBlock() {
        DrumSequencerBlock drumSequencer = new DrumSequencerBlock(receiver);
        clock.add(drumSequencer);
        table.getChildren().add(drumSequencer);

        reorderOnFocus(drumSequencer);
    }

    @FXML
    void loadSetup() {
        String setupName = setupNameField.getText();
        setupName = setupName.trim();
        try {
            Collection<Database.Block> blocks = getSetup(setupName);
            clock.clear();
            playgroundSound.bind(new DC(0));
            for (Node node : table.getChildren())
                if (node instanceof Deletable deletable)
                    deletable.onDelete();
            table.getChildren().clear();
            for (Database.Block block : blocks) {
                Node addedBlock;
                switch (block.type()) {
                    case "keyboard" -> {
                        createKeyboardBlock();
                        addedBlock = table.getChildren().get(table.getChildren().size() - 1);
                    }
                    case "chord machine" -> {
                        createChordMachineBlock();
                        addedBlock = table.getChildren().get(table.getChildren().size() - 1);
                    }
                    case "drum sequencer" -> {
                        createDrumSequencerBlock();
                        addedBlock = table.getChildren().get(table.getChildren().size() - 1);
                    }
                    case "synth" -> {
                        Database.SynthBlock synthBlock = (Database.SynthBlock) block;
                        createSynthBlock(synthBlock.synth(), PolyphonyUtils.byString(synthBlock.polyphony()));
                        addedBlock = table.getChildren().get(table.getChildren().size() - 1);
                        if (synthBlock.patch() != null)
                            ((SynthBlock) addedBlock).loadPatch(synthBlock.patch());
                        ((SynthBlock) addedBlock).setVolume(synthBlock.volume());
                    }
                    default -> throw new RuntimeException();
                }

                addedBlock.setTranslateX(block.x());
                addedBlock.setTranslateY(block.y());
            }
        } catch (NoSuchSetupException e) {
            messageText.setText("no such setup \"" + setupName + "\"");
        } catch (PolyphonyException | NoSuchSynthException | StructScriptException e){
            messageText.setText("setup contains incorrect data");
        }
    }

    void saveSetup(String setupName){
        List<Database.Block> blocks = new ArrayList<>();
        for(Node node : table.getChildren()){
            int x = (int) node.getTranslateX(),
                y = (int) node.getTranslateY();
            if(node instanceof KeyboardBlock)
                blocks.add(new Database.Block("keyboard", x, y));
            else if(node instanceof DrumSequencerBlock)
                blocks.add(new Database.Block("drum sequencer", x, y));
            else if(node instanceof ChordMachineBlock)
                blocks.add(new Database.Block("chord machine", x, y));
            else if(node instanceof SynthBlock synthBlock){
                blocks.add(new Database.SynthBlock(x, y, synthBlock.getSynthName(), synthBlock.getChosenPatch(), synthBlock.getPolyphony().getShortName(), synthBlock.getVolume()));
            }
        }
        Database.saveSetup(setupName, clock.getBPM(), blocks);
        messageText.setText("setup successfully saved");
    }

    @FXML
    void saveSetup(){
        saveSetup(setupNameField.getText());
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
            BPMspinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999));
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
