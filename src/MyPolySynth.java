import sources.Gated;
import sources.SignalSource;
import sources.utils.Mixer;
import sources.utils.MultiGate;
import sources.utils.SourceValue;
import sources.voices.Voice;

import java.util.*;

public class MyPolySynth implements Synth {
    Voice[] voices;
    Map<Integer, Voice> voiceByNote = new HashMap<>();
    Set<Voice> freeVoices = new LinkedHashSet<>(), releasedVoices = new LinkedHashSet<>(), gatedVoices = new LinkedHashSet<>();
    SignalSource soundSource;
    LinkedList<SourceValue> valuesToMap = new LinkedList<>();
    Map<Integer, SourceValue> valueByCC = new HashMap<>();

    Gated paraphonicGated;

    public MyPolySynth(Voice[] voices) {
        this(voices, new Mixer(voices));
    }

    public MyPolySynth(Voice[] voices, SignalSource soundSource) {
        this(voices, soundSource, new MultiGate());
    }

    public MyPolySynth(Voice[] voices, SignalSource soundSource, Gated paraphonicGated) {
        this.voices = voices;
        this.soundSource = soundSource;
        this.paraphonicGated = paraphonicGated;
        printTryingToMap();
        freeVoices.addAll(Arrays.asList(voices));
    }

    private void printTryingToMap() {
        if (valuesToMap.isEmpty())
            return;
        System.out.println("Mapping \"" + valuesToMap.getFirst().getDescription() + "\"");
    }

    @Override
    public void noteOn(int note, int velocity) {
        paraphonicGated.gateOn();
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
        if (gatedVoices.isEmpty())
            paraphonicGated.gateOff();
    }

    @Override
    public void midiCC(int CC, int value) {
        if (!valueByCC.containsKey(CC)) {
            if (valuesToMap.isEmpty())
                return;
            valueByCC.put(CC, valuesToMap.removeFirst());
            System.out.println("\"" + valueByCC.get(CC).getDescription() + "\" mapped to CC#" + CC);
            printTryingToMap();
        }
        valueByCC.get(CC).setValue(value / 128.0);
    }


    @Override
    public double getSample(int sampleId) {
        return soundSource.getSample(sampleId);
    }
}
