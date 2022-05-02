package ui.gui;

import database.NoSuchSynthException;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
import java.util.List;

import static database.Database.getSynthStructure;
import static database.Database.saveSynth;

public class MainGUI extends Application{

    public static void debugBorder(Region region){
        region.setBorder(new Border(new BorderStroke(Color.GREY,
                BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    public static void main(String[] args){
        launch(args);
    }

    private SoundPlayer player = null;
    private SynthMidiReceiver<SynthController> receiver = null;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Synth");
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
        VBox root = new VBox(codeField, codeControls);

        SourceValue masterVolume = new SourceValue("masterVolume", 0.1);
        Socket sound = new Socket();
        player = new SoundPlayer(sound.attenuate(masterVolume));

        receiver = new SynthMidiReceiver<>();
        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            try {
                device = MidiSystem.getMidiDevice(info);
                List<Transmitter> transmitters = device.getTransmitters();
                for (Transmitter transmitter : transmitters)
                    transmitter.setReceiver(receiver);

                Transmitter trans = device.getTransmitter();
                trans.setReceiver(receiver);
                device.open();
            } catch (MidiUnavailableException e) {
                //System.out.println("Device " + i + " error");
            }
        }

        player.play();
        loadButton.setOnAction(event->{
            String name = synthNameField.getCharacters().toString();
            try{
                codeField.setText(getSynthStructure(name));
                messageText.setText("synth loaded successfully");
            } catch (NoSuchSynthException e) {
                messageText.setText("no such synth \"" + name + "\"");
            }
        });
        saveButton.setOnAction(event -> {
            String name = synthNameField.getCharacters().toString();
            if(name.isBlank()) {
                messageText.setText("enter synths name to save it");
                return;
            }
            String code = codeField.getText();
            saveSynth(name, code);
            messageText.setText("synth \"" + name + "\" saved successfully");
        });
        buildButton.setOnAction(event -> {
            try {
                int voiceCount = Integer.parseInt(voiceCountField.getCharacters().toString());
                String structure = codeField.getText();
                Interpreter interpreter = new Interpreter(voiceCount);
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
            }
        });

        stage.setScene(new Scene(root, 720, 540));
        stage.show();
    }

    @Override
    public void stop() {
        if(player != null)
            player.stop();
        if(receiver != null)
            receiver.close();
    }
}