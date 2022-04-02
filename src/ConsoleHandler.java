import sources.SignalSource;
import sources.utils.Mixer;
import synths.Synth;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

public class ConsoleHandler {

    final int channels = 16;

    int editedChannel = -1;
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
        if(command.startsWith("#"))
            return;
        if(command.equals("===")){
            editedChannel = -1;
            return;
        }
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
        if(editedChannel == -1){
            System.out.println("choose channel to edit first");
            return;
        }
        if(command.matches("press +[0-9]+")){
            int note = Integer.parseInt(command.substring(5).trim());
            if(note < 0 || note > 127){
                System.out.println("wrong note");
                return;
            }
            for(Synth synth : synths[editedChannel])
                synth.noteOn(note);
            return;
        }
        if(command.matches("depress")){
            for(Synth synth : synths[editedChannel])
                synth.allNotesOff();
            return;
        }
        if(command.matches("depress +[0-9]+")){
            int note = Integer.parseInt(command.substring(7).trim());
            if(note < 0 || note > 127){
                System.out.println("wrong note");
                return;
            }
            for(Synth synth : synths[editedChannel])
                synth.noteOff(note);
            return;
        }
        if (command.matches("create +[0-9]+")) {
            try {
                int voiceCount = Integer.parseInt(command.substring(6).trim());
                if (voiceCount < 0) {
                    System.out.println("wrong voice count");
                    return;
                }
                builders[editedChannel] = new SynthBuilder(voiceCount);
                mix.get(editedChannel).bind(builders[editedChannel].getSynth());
                synths[editedChannel] = new Synth[]{builders[editedChannel].getSynth()};
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
        try {
            if(command.equals("history")){
                System.out.println(String.join("\n", builders[editedChannel].getHistory()));
                return;
            }
            builders[editedChannel].handleCommand(command);
        }catch (IncorrectFormatException e) {
            System.out.println("incorrect format");
        } catch (NoSuchClassException e) {
            System.out.println("no such class \"" + e.getMessage() + "\"");
        } catch (NoSuchObjectException e) {
            System.out.println("no such object \"" + e.getMessage() + "\"");
        } catch (NoSuchSocketException e) {
            System.out.println("no such socket \"" + e.getMessage() + "\"");
        } catch (NoSuchSignalException e) {
            System.out.println("no such signal \"" + e.getMessage() + "\"");
        } catch (NoSuchMethodException e){
            System.out.println("no such method \"" + e.getMessage() + "\"");
        } catch (NoSuchConstructorException e) {
            System.out.println("no such constructor");
        } catch (IsNotAProcessorException e) {
            System.out.println(e.getMessage() + " is not a processor");
        } catch (VoiceAndGlobalInteractionException e) {
            System.out.println("voice things doing things with global things");
        } catch (NumberFormatException e){
            System.out.println("value expected");
        } catch (FileNotFoundException e) {
            System.out.println("loading file error");
        }
    }

    SignalSource getMix() {
        return clippedMix;
    }
}
