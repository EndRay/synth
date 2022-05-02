package ui.cui;

import database.NoSuchSynthException;
import sequencer.Sequencer;
import synthesizer.TimeDependent;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Mixer;
import synthesizer.sources.utils.SourceValue;
import ui.structscript.Interpreter;
import ui.structscript.StructScriptException;
import ui.synthcontrollers.AutoMapSynthController;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static database.Database.getSynthStructure;
import static java.lang.Math.min;
import static javax.sound.midi.MidiSystem.getSequence;
import static ui.SynthMidiReceiver.channels;

public class ConsoleHandler implements TimeDependent {

    int editedChannel = -1;
    AutoMapSynthMidiReceiver midiReceiver;
    Interpreter[] builders = new Interpreter[channels];
    Sequencer[] sequencers = new Sequencer[channels];

    SourceValue mixGain = new SourceValue("mix gain", 0.5);
    SourceValue masterVolume = new SourceValue("master volume", 0.3);
    Mixer mix = new Mixer(channels);
    SignalSource clippedMix = mix.attenuate(mixGain).clipBi().attenuate(masterVolume);


    public void samplePassed(){
        midiReceiver.samplePassed();
    }

    ConsoleHandler() {
        midiReceiver = new AutoMapSynthMidiReceiver();
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
        if (command.matches("master volume set +[0-9]+(.[0-9]+)?")){
            double new_volume = Double.parseDouble(command.substring(17).trim());
            new_volume = min(new_volume, 1);
            masterVolume.setValue(new_volume);
            return;
        }
        if(editedChannel == -1){
            System.out.println("choose channel to edit first");
            return;
        }
        if(command.matches("\\[ *(-?[0-9]+ *, *)*-?[0-9]+ *]")){
            List<Integer> notes = Arrays.stream(command.substring(1, command.length() - 1).trim().split(" *, *")).map(Integer::valueOf).toList();
            sequencers[editedChannel].play(notes);
            return;
        }
//        if(command.matches("press +[0-9]+")){
//            int note = Integer.parseInt(command.substring(5).trim());
//            if(note < 0 || note > 127){
//                System.out.println("wrong note");
//                return;
//            }
//            for(SynthController synth : synths.get(editedChannel))
//                synth.noteOn(note);
//            return;
//        }
//        if(command.matches("depress")){
//            for(SynthController synth : synths.get(editedChannel))
//                synth.allNotesOff();
//            return;
//        }
//        if(command.matches("depress +[0-9]+")){
//            int note = Integer.parseInt(command.substring(7).trim());
//            if(note < 0 || note > 127){
//                System.out.println("wrong note");
//                return;
//            }
//            for(SynthController synth : synths.get(editedChannel))
//                synth.noteOff(note);
//            return;
//        }
        if (command.matches("create +[0-9]+")) {
            try {
                int voiceCount = Integer.parseInt(command.substring(6).trim());
                if (voiceCount < 0) {
                    System.out.println("wrong voice count");
                    return;
                }
                CCSourceValuesHandler handler = new CCSourceValuesHandler();
                builders[editedChannel] = new Interpreter(voiceCount, handler);
                mix.get(editedChannel).bind(builders[editedChannel].getVoiceDistributor());
                midiReceiver.clearSynthControllers(editedChannel);
                midiReceiver.addSynthController(editedChannel, new AutoMapSynthController(builders[editedChannel].getVoiceDistributor(), handler));
                return;
            } catch (NumberFormatException e) {
                System.out.println("wrong voice count format");
                return;
            }
        }
        if (command.matches("map")) {
            midiReceiver.startMapping(editedChannel);
            return;
        }
        if (command.matches("stop +map")) {
            midiReceiver.stopMapping(editedChannel);
            return;
        }
        if (builders[editedChannel] == null) {
            System.out.println("synth on channel " + (editedChannel + 1) + " is not created");
            return;
        }
        if(command.matches("load +\".*\"")){
            String name = command.substring(5).trim().substring(1);
            name = name.substring(0, name.length()-1);
            try {
                builders[editedChannel].run(getSynthStructure(name));
            } catch (NoSuchSynthException e) {
                System.out.println("no such synth \"" + name + "\"");
            } catch (StructScriptException e) {
                System.out.println("error occurred in synth \"" + name + "\"");
                System.out.println(e.getStructScriptMessage());
            }
            return;
        }
        try {
            builders[editedChannel].run(command);
        } catch (StructScriptException e) {
            System.out.println(e.getStructScriptMessage());
        }
    }

    SignalSource getMix() {
        return clippedMix;
    }
}
