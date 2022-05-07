package ui.gui;

import database.NoSuchPatchException;
import database.NoSuchSynthException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import synthesizer.SoundPlayer;
import synthesizer.VoiceDistributor;
import synthesizer.sources.utils.Socket;
import synthesizer.sources.utils.SourceValue;
import midi.SynthMidiReceiver;
import structscript.Interpreter;
import structscript.StructScriptException;
import ui.synthcontrollers.SimpleSynthController;
import ui.synthcontrollers.SynthController;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.Database.*;

public class MainGUI extends Application {

    public static void debugBorder(Region region) {
        region.setBorder(new Border(new BorderStroke(Color.GREY,
                BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    public static void main(String[] args) {
        launch(args);
    }

    private SoundPlayer player = null;
    private SynthMidiReceiver<SynthController> receiver = null;

    private List<MidiDevice> midiDevices = null;

    private TextField synthNameField;
    private TextField messageText;

    private ObservableList<Value> values;
    private final BooleanProperty leftPatchEditing = new SimpleBooleanProperty(true);
    private final BooleanProperty rightPatchEditing = new SimpleBooleanProperty(false);
    private final Map<String, Double> leftPatch = new HashMap<>(), rightPatch = new HashMap<>();
    private Map<String, Double> morphPatch(double morph){
        Map<String, Double> res = new HashMap<>();
        for(Value value : values)
            if(value.value() != null)
                res.put(value.name(), leftPatch.get(value.name()) * (1 - morph) + rightPatch.get(value.name()) * morph);
        return res;
    }
    private void assignMorphValues(double morph){
        Map<String, Double> patch = morphPatch(morph);
        for(Value value : values)
            if(value.value() != null && patch.containsKey(value.name()))
                value.value().set(patch.get(value.name()));
    }

    Region createStructureEnvironment(Socket sound, PropertiesSourceValuesHandler handler) {
        TextArea structureField = new TextArea();
        structureField.setFont(Font.font("Monospaced", 16));
        VBox.setVgrow(structureField, Priority.ALWAYS);
        Button loadButton = new Button("load");
        synthNameField = new TextField();
        synthNameField.setFont(Font.font("Monospaced", 14));
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        TextField voiceCountField = new TextField("6");
        voiceCountField.setFont(Font.font("Monospaced", 14));
        voiceCountField.setPrefWidth(50);
        Button saveButton = new Button("save");
        Button buildButton = new Button("build");
        HBox structureManager = new HBox(loadButton, synthNameField, saveButton, space, voiceCountField, buildButton);
        structureManager.setAlignment(Pos.CENTER);

        loadButton.setOnAction(event -> {
            String name = synthNameField.getCharacters().toString();
            try {
                structureField.setText(getSynthStructure(name));
                messageText.setText("synth \"" + name + "\" loaded successfully");
            } catch (NoSuchSynthException e) {
                messageText.setText("no such synth \"" + name + "\"");
            }
        });
        saveButton.setOnAction(event -> {
            String name = synthNameField.getCharacters().toString();
            String code = structureField.getText();
            saveSynth(name, code);
            messageText.setText("synth \"" + name + "\" saved successfully");
        });
        buildButton.setOnAction(event -> {
            String structure = structureField.getText();
            try {
                int voiceCount = Integer.parseInt(voiceCountField.getCharacters().toString());
                handler.resetValues();
                Interpreter interpreter = new Interpreter(voiceCount, handler);
                interpreter.run(structure);
                VoiceDistributor distributor = interpreter.getVoiceDistributor();
                sound.bind(distributor);
                receiver.clearSynthControllers(0);
                receiver.addSynthController(0, new SimpleSynthController(distributor));
                messageText.setText("synth built successfully");
            } catch (NumberFormatException e) {
                messageText.setText("voice count must be an integer");
            } catch (StructScriptException e) {
                messageText.setText(e.getStructScriptMessage());
                int linePos = 0;
                for (int i = 0; i < e.getLine() - 1; ++i)
                    linePos = structure.indexOf('\n', linePos) + 1;
                int linePosTo = structure.indexOf('\n', linePos);
                structureField.positionCaret(linePos);
                if (linePosTo != -1)
                    structureField.selectPositionCaret(linePosTo);
                else structureField.selectEnd();
            }
        });


        return new VBox(structureField, structureManager);
    }

    Region createPatchEnvironment() {
        Button leftLoadButton = new Button("load");
        TextField leftPatchNameField = new TextField();
        leftPatchNameField.setFont(Font.font("Monospaced", 14));
        Button leftSaveButton = new Button("save");

        Slider morphSlider = new Slider();
        morphSlider.setMin(0);
        morphSlider.setMax(1);
        morphSlider.setValue(0);
        HBox.setHgrow(morphSlider, Priority.ALWAYS);

        Button rightLoadButton = new Button("load");
        TextField rightPatchNameField = new TextField();
        rightPatchNameField.setFont(Font.font("Monospaced", 14));
        Button rightSaveButton = new Button("save");


        ListView<Region> listView = new ListView<>();
        ObservableList<Region> list = FXCollections.observableArrayList();
        listView.setItems(list);
        values.addListener((ListChangeListener<Value>) change -> {
            list.clear();
            for (Value value : change.getList()) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER);
                if (value.value() == null) {
                    Text text = new Text(value.name());
                    text.setFont(Font.font("", FontWeight.BOLD, 20));

                    row.getChildren().addAll(text);
                } else {
                    value.value().addListener((observable, oldValue, newValue) -> {
                        if(leftPatchEditing.get())
                            leftPatch.put(value.name(), newValue.doubleValue());
                        if(rightPatchEditing.get())
                            rightPatch.put(value.name(), newValue.doubleValue());
                    });
                    if(!leftPatch.containsKey(value.name()))
                        leftPatch.put(value.name(), value.value().doubleValue());
                    if(!rightPatch.containsKey(value.name()))
                        rightPatch.put(value.name(), value.value().doubleValue());

                    Slider slider = new Slider();
                    slider.setMin(0);
                    slider.setMax(1);
                    slider.valueProperty().bindBidirectional(value.value());
                    slider.setMajorTickUnit(0.5);
                    slider.setMinorTickCount(5);
                    slider.setShowTickMarks(true);
                    slider.disableProperty().bind(leftPatchEditing.or(rightPatchEditing).not());
                    Text text = new Text(value.name());
                    text.setFont(Font.font(16));
                    VBox textBox = new VBox(text);
                    textBox.setPrefWidth(150);
                    textBox.setAlignment(Pos.CENTER);
                    HBox.setHgrow(slider, Priority.ALWAYS);
                    row.getChildren().addAll(slider, textBox);
                }
                list.add(row);
            }
            assignMorphValues(morphSlider.getValue());
        });
        VBox.setVgrow(listView, Priority.ALWAYS);

        HBox patchManager = new HBox(leftLoadButton, leftPatchNameField, leftSaveButton, morphSlider, rightLoadButton, rightPatchNameField, rightSaveButton);
        patchManager.setAlignment(Pos.CENTER);

        leftLoadButton.setOnAction(event -> {
            String synth = synthNameField.getText();
            String patch = leftPatchNameField.getText();
            try {
                Map<String, Double> newPatch = getPatch(synth, patch);
                leftPatch.putAll(newPatch);
                assignMorphValues(morphSlider.getValue());
                messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" loaded successfully");
            } catch (NoSuchPatchException e) {
                messageText.setText("synth \"" + synth + "\" has no such patch \"" + patch + "\"");
            }
        });
        leftSaveButton.setOnAction(event -> {
            String synth = synthNameField.getText();
            String patch = leftPatchNameField.getText();
            try {
                Map<String, Double> parameters = new HashMap<>();
                for(Value value : values)
                    if(value.value() != null)
                        parameters.put(value.name(), value.value().getValue());
                savePatch(synth, patch, parameters);
                messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" saved successfully");
            } catch (NoSuchSynthException e) {
                messageText.setText("no such synth \"" + synth + "\"");
            }
        });

        morphSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            leftPatchEditing.set(newValue.doubleValue() == 0);
            rightPatchEditing.set(newValue.doubleValue() == 1);
            assignMorphValues(newValue.doubleValue());
        });

        rightLoadButton.setOnAction(event -> {
            String synth = synthNameField.getText();
            String patch = rightPatchNameField.getText();
            try {
                Map<String, Double> newPatch = getPatch(synth, patch);
                rightPatch.putAll(newPatch);
                assignMorphValues(morphSlider.getValue());
                messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" loaded successfully");
            } catch (NoSuchPatchException e) {
                messageText.setText("synth \"" + synth + "\" has no such patch \"" + patch + "\"");
            }
        });
        rightSaveButton.setOnAction(event -> {
            String synth = synthNameField.getText();
            String patch = rightPatchNameField.getText();
            try {
                Map<String, Double> parameters = new HashMap<>();
                for(Value value : values)
                    if(value.value() != null)
                        parameters.put(value.name(), value.value().getValue());
                savePatch(synth, patch, parameters);
                messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" saved successfully");
            } catch (NoSuchSynthException e) {
                messageText.setText("no such synth \"" + synth + "\"");
            }
        });
        return new VBox(listView, patchManager);
    }

    Region createSettingsEnvironment(SourceValue masterVolume) {
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(1);
        slider.setValue(masterVolume.getValue());
        slider.valueProperty().addListener((observable, oldValue, newValue) -> masterVolume.setValue(newValue.doubleValue()));

        slider.setOrientation(Orientation.VERTICAL);
        slider.setPrefHeight(300);
        VBox box = new VBox(slider);
        box.setAlignment(Pos.TOP_CENTER);
        return box;
    }

    Region createBottomThing() {
        Text text = new Text("HERE KEYBOARD/SEQUENCER");
        text.setFont(Font.font(40));
        HBox bottomThing = new HBox(text);
        bottomThing.setAlignment(Pos.CENTER);
        bottomThing.setPrefHeight(200);
        bottomThing.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        return bottomThing;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Synth");

        SourceValue masterVolume = new SourceValue("masterVolume", 0.2);
        Socket sound = new Socket();
        player = new SoundPlayer(sound, masterVolume);

        receiver = new SynthMidiReceiver<>();
        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        midiDevices = new ArrayList<>();
        for (MidiDevice.Info info : infos) {
            try {
                device = MidiSystem.getMidiDevice(info);
                List<Transmitter> transmitters = device.getTransmitters();
                for (Transmitter transmitter : transmitters)
                    transmitter.setReceiver(receiver);

                Transmitter trans = device.getTransmitter();
                trans.setReceiver(receiver);
                device.open();
                midiDevices.add(device);
            } catch (MidiUnavailableException e) {
                //System.out.println("Device " + i + " error");
            }
        }

        player.play();

        PropertiesSourceValuesHandler sourceValuesHandler = new PropertiesSourceValuesHandler();
        values = sourceValuesHandler.getValues();

        messageText = new TextField("synth not built yet");
        messageText.setEditable(false);
        messageText.setFont(Font.font("Monospaced", 14));
        messageText.setStyle("-fx-text-fill: grey");
        messageText.setAlignment(Pos.CENTER_LEFT);
        messageText.setPrefWidth(0);
        Region bottomThing = createBottomThing();
        Region structEnvironment = createStructureEnvironment(sound, sourceValuesHandler);
        Region controlsEnvironment = createPatchEnvironment();
        Region settingsEnvironment = createSettingsEnvironment(masterVolume);
        structEnvironment.setPrefWidth(600);
        settingsEnvironment.setPrefWidth(30);
        HBox.setHgrow(controlsEnvironment, Priority.ALWAYS);
        HBox topThing = new HBox(structEnvironment, controlsEnvironment, settingsEnvironment);
        VBox.setVgrow(topThing, Priority.ALWAYS);
        VBox root = new VBox(topThing, messageText, bottomThing);

        stage.setScene(new Scene(root, 1280, 720));
        stage.show();
    }

    @Override
    public void stop() {
        if (player != null)
            player.stop();
        if (receiver != null)
            receiver.close();
        if (midiDevices != null)
            for (MidiDevice device : midiDevices)
                device.close();
        Platform.exit();
    }
}
