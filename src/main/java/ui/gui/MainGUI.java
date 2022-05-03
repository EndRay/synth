package ui.gui;

import database.NoSuchSynthException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SpotLight;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
import java.util.List;

import static database.Database.getSynthStructure;
import static database.Database.saveSynth;

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

    Region createStructEnvironment(Socket sound, PropertiesSourceValuesHandler handler) {
        TextArea codeField = new TextArea();
        codeField.setFont(Font.font("Monospaced", 16));
        VBox.setVgrow(codeField, Priority.ALWAYS);
        Button loadButton = new Button("load");
        TextField synthNameField = new TextField();
        synthNameField.setFont(Font.font("Monospaced", 14));
        TextField messageText = new TextField("synth not built yet");
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
        HBox codeControls = new HBox(loadButton, synthNameField, saveButton, messageText, voiceCountField, buildButton);
        codeControls.setAlignment(Pos.CENTER_LEFT);

        loadButton.setOnAction(event -> {
            String name = synthNameField.getCharacters().toString();
            try {
                codeField.setText(getSynthStructure(name));
                messageText.setText("synth loaded successfully");
            } catch (NoSuchSynthException e) {
                messageText.setText("no such synth \"" + name + "\"");
            }
        });
        saveButton.setOnAction(event -> {
            String name = synthNameField.getCharacters().toString();
            if (name.isBlank()) {
                messageText.setText("enter synths name to save it");
                return;
            }
            String code = codeField.getText();
            saveSynth(name, code);
            messageText.setText("synth \"" + name + "\" saved successfully");
        });
        buildButton.setOnAction(event -> {
            String structure = codeField.getText();
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
                codeField.positionCaret(linePos);
                if (linePosTo != -1)
                    codeField.selectPositionCaret(linePosTo);
                else codeField.selectEnd();
            }
        });


        return new VBox(codeField, codeControls);
    }

    Region createControlsEnvironment(ObservableList<Value> values) {
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
        return listView;
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
        Region structEnvironment = createStructEnvironment(sound, sourceValuesHandler);
        Region controlsEnvironment = createControlsEnvironment(sourceValuesHandler.getValues());
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
