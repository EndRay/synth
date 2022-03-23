import sources.SignalSource;
import sources.utils.Mixer;
import sources.voices.Voice;

import java.util.*;

public class MyPolySynth implements Synth {
    Voice[] voices;
    Map<Integer, Voice> voiceByNote;
    Set<Voice> freeVoices, releasedVoices, gatedVoices;
    SignalSource mixer;

    public MyPolySynth(Voice[] voices){
        this.voices = voices;
        mixer = new Mixer(voices);
        voiceByNote = new HashMap<>();
        freeVoices = new LinkedHashSet<>();
        freeVoices.addAll(Arrays.asList(voices));
        releasedVoices = new LinkedHashSet<>();
        gatedVoices = new LinkedHashSet<>();
    }

    @Override
    public void noteOn(int note, int velocity) {
        Voice voice;
        if(!freeVoices.isEmpty()) {
            Iterator<Voice> it = freeVoices.iterator();
            voice = it.next();
            it.remove();
        }
        else if(!releasedVoices.isEmpty()){
            Iterator<Voice> it = releasedVoices.iterator();
            voice = it.next();
            it.remove();
            voice.noteOff();
        }
        else{
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
        if(voiceByNote.containsKey(note)) {
            Voice voice = voiceByNote.remove(note);
            gatedVoices.remove(voice);
            releasedVoices.add(voice);
            voice.noteOff(velocity);
        }
    }


    @Override
    public double getSample(int sampleId) {
        return mixer.getSample(sampleId);
    }
}
