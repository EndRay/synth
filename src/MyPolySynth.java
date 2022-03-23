import sources.SignalSource;
import sources.utils.Mixer;
import sources.utils.SourceValue;
import sources.voices.Voice;

import java.util.*;

public class MyPolySynth implements Synth {
    Voice[] voices;
    Map<Integer, Voice> voiceByNote;
    Set<Voice> freeVoices, releasedVoices, gatedVoices;
    SignalSource soundSource;
    LinkedList<SourceValue> valuesToMap;
    Map<Integer, SourceValue> valueByCC;

    public MyPolySynth(Voice[] voices) {
        this(voices, new Mixer(voices));
    }

    public MyPolySynth(Voice[] voices, SourceValue[] values) {
        this(voices, new Mixer(voices), values);
    }

    public MyPolySynth(Voice[] voices, SignalSource soundSource) {
        this(voices, soundSource, new SourceValue[0]);
    }

    public MyPolySynth(Voice[] voices, SignalSource soundSource, SourceValue[] values) {
        this.voices = voices;
        this.soundSource = soundSource;
        valueByCC = new HashMap<>();
        valuesToMap = new LinkedList<>(List.of(values));
        printTryingToMap();
        voiceByNote = new HashMap<>();
        freeVoices = new LinkedHashSet<>();
        freeVoices.addAll(Arrays.asList(voices));
        releasedVoices = new LinkedHashSet<>();
        gatedVoices = new LinkedHashSet<>();
    }

    private void printTryingToMap(){
        if(valuesToMap.isEmpty())
            return;
        System.out.println("Mapping \"" + valuesToMap.getFirst().getDescription() + "\"");
    }

    @Override
    public void noteOn(int note, int velocity) {
        Voice voice;
        if (!freeVoices.isEmpty()) {
            Iterator<Voice> it = freeVoices.iterator();
            voice = it.next();
            it.remove();
        } else if (!releasedVoices.isEmpty()) {
            Iterator<Voice> it = releasedVoices.iterator();
            voice = it.next();
            it.remove();
            voice.noteOff();
        } else {
            Iterator<Voice> it = gatedVoices.iterator();
            voice = it.next();
            it.remove();
            voice.noteOff();
        }
        voiceByNote.values().remove(voice);
        voice.noteOn(note, velocity);
        gatedVoices.add(voice);
        voiceByNote.put(note, voice);
    }

    @Override
    public void noteOff(int note, int velocity) {
        if (voiceByNote.containsKey(note)) {
            Voice voice = voiceByNote.remove(note);
            gatedVoices.remove(voice);
            releasedVoices.add(voice);
            voice.noteOff(velocity);
        }
    }

    @Override
    public void midiCC(int CC, int value) {
        if(!valueByCC.containsKey(CC)){
            if(valuesToMap.isEmpty())
                return;
            valueByCC.put(CC, valuesToMap.removeFirst());
            System.out.println("\"" + valueByCC.get(CC).getDescription() + "\" mapped to CC#" + CC);
            printTryingToMap();
        }
        valueByCC.get(CC).setValue(value/128.0);
    }


    @Override
    public double getSample(int sampleId) {
        return soundSource.getSample(sampleId);
    }
}
