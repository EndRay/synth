package ui.cui;

import sequencer.Sequencer;
import synthesizer.Synth;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Mixer;
import ui.SynthMidiReceiver;
import ui.structscript.Interpreter;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static javax.sound.midi.MidiSystem.getSequence;

public class ConsoleHandler {

    final int channels = 16;

    int editedChannel = -1;
    SynthMidiReceiver midiReceiver;
    Interpreter[] builders = new Interpreter[channels];
    Sequencer[] sequencers = new Sequencer[channels];

    Synth[][] synths = new Synth[channels][];

    {
        for (int i = 0; i < channels; ++i)
            synths[i] = new Synth[0];
    }

    Mixer mix = new Mixer(channels);
    SignalSource clippedMix = mix.clipBi();


    public void samplePassed(){
        midiReceiver.samplePassed();
    }

    ConsoleHandler() {
        midiReceiver = new SynthMidiReceiver(synths);
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
        for(int i = 0; i < channels; ++i){
            sequencers[i] = new Sequencer(midiReceiver, i);
        }
    }

    void handleCommand(String command) {
        if (command.isBlank())
            return;
        command = command.trim();
        if(command.startsWith("#"))
            return;
        if(command.matches("==+=")){
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
        if (command.matches("play .*")) {
            File midiFile = new File(command.substring(5).trim());
            try {
                Sequence sequence = getSequence(midiFile);
                midiReceiver.playSequence(sequence);
            } catch (InvalidMidiDataException | IOException e) {
                System.out.println("invalid midi data");
            }
            return;
        }
        if(editedChannel == -1){
            System.out.println("choose channel to edit first");
            return;
        }
        if(command.matches("\\[ *([0-9]+ *, *)*[0-9]+ *]")){
            List<Integer> notes = Arrays.stream(command.substring(1, command.length() - 1).trim().split(" *, *")).map(Integer::valueOf).toList();
            sequencers[editedChannel].play(notes);
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
                builders[editedChannel] = new Interpreter(voiceCount);
                mix.get(editedChannel).bind(builders[editedChannel].getSynth());
                synths[editedChannel] = new Synth[]{builders[editedChannel].getSynth()};
                return;
            } catch (NumberFormatException e) {
                System.out.println("wrong voice count format");
                return;
            }
        }
        if (command.matches("map")) {
            for(Synth synth : synths[editedChannel])
                synth.startMapping();
            return;
        }
        if (command.matches("stop +map")) {
            for(Synth synth : synths[editedChannel])
                synth.stopMapping();
            return;
        }
        if (builders[editedChannel] == null) {
            System.out.println("synth on channel " + (editedChannel + 1) + " is not created");
            return;
        }
        builders[editedChannel].run(command);
    }

    SignalSource getMix() {
        return clippedMix;
    }
}
