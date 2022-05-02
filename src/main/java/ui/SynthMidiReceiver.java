package ui;

import synthesizer.sources.SignalSource;
import ui.synthcontrollers.SynthController;

import javax.sound.midi.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * !!! ONLY PPQ
 * Common BPM
 *
 * TODO: Parse polyaftertouch
 */

public class SynthMidiReceiver<T extends SynthController> implements Receiver {

    public static final int channels = 16;

    protected List<Collection<T>> synths = new ArrayList<>(channels);

    {
        for (int i = 0; i < channels; ++i)
            synths.add(new ArrayList<>());
    }

    public SynthMidiReceiver() {}

    public record Event(int sampleId, MidiMessage message) implements Comparable<Event> {

        @Override
        public int compareTo(Event o) {
            return Integer.compare(sampleId, o.sampleId);
        }
    }

    public void addSynthController(int channel, T controller){
        synths.get(channel).add(controller);
    }

    public void clearSynthControllers(int channel){
        synths.get(channel).clear();
    }

    double bpm;
    int samplesPassed = 0;
    Queue<Event> sequenced = new ArrayDeque<>();

    public void playSequence(Sequence sequence){
        if(sequence.getDivisionType() != Sequence.PPQ)
            throw new RuntimeException("Wrong division type");
        int trackNumber = 0;
        int ticksPerBeat = sequence.getResolution();
        List<Event> events = new ArrayList<>();
//        int offset = 0;
        for (Track track :  sequence.getTracks()) {
            trackNumber++;
            System.out.println("Track #" + trackNumber);
            Set<Integer> usedChannels = new HashSet<>();
            for (int i=0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage sm)
                    usedChannels.add(sm.getChannel());
                if (message instanceof MetaMessage mm) {
                    int type = mm.getType();
                    byte[] data = mm.getData();
                    switch (type){
                        case 0x03:
                            System.out.println("Track name: " + new String(data, StandardCharsets.UTF_8));
                            break;
                        case 0x51:
                            double microseconds = (data[0]<<16) + (data[1]<<8) + data[0];
                            bpm = 60*1e6/microseconds;
                            System.out.println("BPM: " + bpm);
                            break;
//                        case 0x54:
//                            int minutes = data[0];
//                            int seconds = data[1];
//                            offset = 60*minutes + seconds;
//                            System.out.println("Offset: " + offset + " seconds");
//                            break;
                        default:
                            System.out.println("Unknown meta message with type: " + type);
                            break;
                    }
                }
                else events.add(new Event((int)(event.getTick() / ticksPerBeat / bpm * 60 * SignalSource.sampleRate), message));
            }
            System.out.println("Used channels: " + usedChannels.stream().sorted().map(x -> Integer.toString(x+1)).collect(Collectors.joining(", ")));
        }
        events.sort(Event::compareTo);
        sequenced = new ArrayDeque<>(events);
//        samplesPassed = (offset*SignalSource.sampleRate)-1;
        samplesPassed = -1;
    }

    public void samplePassed(){
        if(sequenced.isEmpty())
            return;
        ++samplesPassed;
        while(!sequenced.isEmpty() && sequenced.peek().sampleId <= samplesPassed){
            Event event = sequenced.poll();
            send(event.message(), 0);
        }
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        byte[] mArr = message.getMessage();
        byte channel = (byte) (mArr[0]&((1<<4)-1)),
             action = (byte) (mArr[0]>>4);
        if(action == -7 && mArr[2] == 0)
            action = -8;
        switch (action) {
            case -5:
                for(SynthController synth : synths.get(channel))
                    synth.midiCC(mArr[1], mArr[2]);
                break;
            case -7:
                for (SynthController synth : synths.get(channel))
                    synth.noteOn(mArr[1], mArr[2]);
                break;
            case -8:
                for (SynthController synth : synths.get(channel))
                    synth.noteOff(mArr[1], mArr[2]);
                break;
        }
    }

    @Override
    public void close() {
        System.out.println("MidiReceiver closing");
    }
}
