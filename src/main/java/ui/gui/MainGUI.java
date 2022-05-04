package ui.gui;

import database.NoSuchPatchException;
import database.NoSuchSynthException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
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
import ui.SynthMidiReceiver;
import ui.structscript.Interpreter;
import ui.structscript.StructScriptException;
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
    Region createStructureEnvironment(Socket sound, PropertiesSourceValuesHandler handler) {
        TextArea structureField = new TextArea();
        structureField.setFont(Font.font("Monospaced", 16));
        VBox.setVgrow(structureField, Priority.ALWAYS);
        Button loadButton = new Button("load");
        synthNameField = new TextField();
        synthNameField.setFont(Font.font("Monospaced", 14));
        messageText = new TextField("synth not built yet");
        messageText.setEditable(false);
        messageText.setFont(Font.font("Monospaced", 14));
        messageText.setStyle("-fx-text-fill: grey");
        messageText.setAlignment(Pos.CENTER_LEFT);
        messageText.setPrefWidth(0);
        HBox.setHgrow(messageText, Priority.ALWAYS);
        TextField voiceCountField = new TextField("6");
        voiceCountField.setFont(Font.font("Monospaced", 14));
        voiceCountField.setPrefWidth(50);
        Button saveButton = new Button("save");
        Button buildButton = new Button("build");
        HBox structureManager = new HBox(loadButton, synthNameField, saveButton, messageText, voiceCountField, buildButton);
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

    Region createPatchEnvironment(ObservableList<Value> values) {
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
                    Slider slider = new Slider();
                    slider.setMin(0);
                    slider.setMax(1);
                    slider.valueProperty().bindBidirectional(value.value());
                    slider.setMajorTickUnit(0.5);
                    slider.setMinorTickCount(5);
                    slider.setShowTickMarks(true);
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
        });
        VBox.setVgrow(listView, Priority.ALWAYS);
        Button loadButton = new Button("load");
        TextField patchNameField = new TextField();
        patchNameField.setFont(Font.font("Monospaced", 14));
        Button saveButton = new Button("save");
        HBox patchManager = new HBox(loadButton, patchNameField, saveButton);
        patchManager.setAlignment(Pos.CENTER);

        loadButton.setOnAction(event -> {
            String synth = synthNameField.getText();
            String patch = patchNameField.getText();
            try {
                Map<String, Double> parameters = getPatch(synth, patch);
                for(Value value : values)
                    if(value.value() != null && parameters.containsKey(value.name()))
                        value.value().setValue(parameters.get(value.name()));
                messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" loaded successfully");
            } catch (NoSuchPatchException e) {
                messageText.setText("synth \"" + synth + "\" has no such patch \"" + patch + "\"");
            }
        });
        saveButton.setOnAction(event -> {
            String synth = synthNameField.getText();
            String patch = patchNameField.getText();
            Map<String, Double> parameters = new HashMap<>();
            for(Value value : values)
                if(value.value() != null)
                    parameters.put(value.name(), value.value().getValue());
            savePatch(synth, patch, parameters);
            messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" saved successfully");
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

        Region bottomThing = createBottomThing();
        Region structEnvironment = createStructureEnvironment(sound, sourceValuesHandler);
        Region controlsEnvironment = createPatchEnvironment(sourceValuesHandler.getValues());
        Region settingsEnvironment = createSettingsEnvironment(masterVolume);
        structEnvironment.setPrefWidth(600);
        settingsEnvironment.setPrefWidth(30);
        HBox.setHgrow(controlsEnvironment, Priority.ALWAYS);
        HBox topThing = new HBox(structEnvironment, controlsEnvironment, settingsEnvironment);
        VBox.setVgrow(topThing, Priority.ALWAYS);
        VBox root = new VBox(topThing, bottomThing);

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
