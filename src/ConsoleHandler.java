import sources.SignalSource;
import sources.utils.Mixer;
import synths.Synth;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import java.util.List;

public class ConsoleHandler {

    final int channels = 16;

    int editedChannel = 0;
    SynthBuilder[] builders = new SynthBuilder[channels];
    Synth[][] synths = new Synth[channels][];

    {
        for (int i = 0; i < channels; ++i)
            synths[i] = new Synth[0];
    }

    Mixer mix = new Mixer(channels);
    SignalSource clippedMix = mix.clipBi();

    ConsoleHandler() {
        SynthMidiReceiver midiReceiver = new SynthMidiReceiver(synths);
        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            try {
                device = MidiSystem.getMidiDevice(info);
                List<Transmitter> transmitters = device.getTransmitters();
                for (Transmitter transmitter : transmitters)
                    transmitter.setReceiver(midiReceiver);

                Transmitter trans = device.getTransmitter();
                trans.setReceiver(midiReceiver);
                device.open();
            } catch (MidiUnavailableException e) {
                //System.out.println("Device " + i + " error");
            }
        }
    }

    void handleCommand(String command) {
        if (command.isBlank())
            return;
        command = command.trim();
        if (command.matches("=[0-9]+=")) {
            try {
                int id = Integer.parseInt(command.substring(1, command.length() - 1).trim()) - 1;
                if (id < 0 || id >= channels) {
                    System.out.println("wrong midi channel");
                    return;
                }
                editedChannel = id;
                return;
            } catch (NumberFormatException e) {
                System.out.println("wrong channel format");
                return;
            }
        }
        if (command.matches("create [0-9]+")) {
            try {
                int voiceCount = Integer.parseInt(command.substring(6).trim());
                if (voiceCount < 1) {
                    System.out.println("wrong voice count");
                    return;
                }
                builders[editedChannel] = new SynthBuilder(voiceCount);
                mix.get(editedChannel).bind(builders[editedChannel].getSynth());
                return;
            } catch (NumberFormatException e) {
                System.out.println("wrong voice count format");
                return;
            }
        }
        if (builders[editedChannel] == null) {
            System.out.println("synth on channel " + (editedChannel + 1) + " is not created");
            return;
        }
        builders[editedChannel].handleCommand(command);
        synths[editedChannel] = new Synth[]{builders[editedChannel].getSynth()};
    }

    SignalSource getMix() {
        return clippedMix;
    }
}
