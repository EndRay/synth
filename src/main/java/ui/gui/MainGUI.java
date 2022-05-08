package ui.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import midi.SynthMidiReceiver;
import synthesizer.SoundPlayer;
import synthesizer.sources.utils.Socket;
import synthesizer.sources.utils.SourceValue;
import ui.synthcontrollers.SynthController;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainGUI extends Application {

    static SourceValue masterVolume;
    static Socket sound;
    static SoundPlayer player;
    static SynthMidiReceiver<SynthController> receiver;
    static List<MidiDevice> midiDevices;

    public static void main(String[] args) {
        masterVolume = new SourceValue("masterVolume", 0.2);
        sound = new Socket();
        player = new SoundPlayer(sound, masterVolume);
        receiver = new SynthMidiReceiver<>();
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        midiDevices = new ArrayList<>();
        for (MidiDevice.Info info : infos) {
            MidiDevice device;
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
        launch(args);
        if (player != null)
            player.stop();
        if (receiver != null)
            receiver.close();
        if (midiDevices != null)
            for (MidiDevice device : midiDevices)
                device.close();
        Platform.exit();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Synth");
        stage.setScene(new Scene(new FXMLLoader(MainGUI.class.getResource("main-menu.fxml")).load()));
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.show();
    }

    @Override
    public void stop() {

    }
}
