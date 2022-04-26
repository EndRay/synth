package synthesizer;

import synthesizer.sources.SignalSource;
import synthesizer.sources.voices.Voice;

import java.util.*;

/**
 * TODO: Correct last note
 */

public class VoiceDistributor implements SignalSource {
    Voice[] voices;
    Map<Integer, Voice> voiceByNote = new HashMap<>();
    Set<Voice> freeVoices = new LinkedHashSet<>(), releasedVoices = new LinkedHashSet<>(), gatedVoices = new LinkedHashSet<>();
    SignalSource soundSource;

    List<Integer> heldNotes = new ArrayList<>();
    Voice last;

    public VoiceDistributor(Voice[] voices, SignalSource soundSource, Voice last) {
        this.voices = voices;
        this.soundSource = soundSource;
        this.last = last;
        freeVoices.addAll(Arrays.asList(voices));
    }

    public void noteOn(int note, int velocity) {
        last.noteOn(note, velocity);
        heldNotes.add(note);
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
        } else if (!gatedVoices.isEmpty()) {
            Iterator<Voice> it = gatedVoices.iterator();
            voice = it.next();
            it.remove();
            voice.noteOff();
        } else return;
        voiceByNote.values().remove(voice);
        voice.noteOn(note, velocity);
        gatedVoices.add(voice);
        voiceByNote.put(note, voice);
    }

    public void allNotesOff(){
        while(!heldNotes.isEmpty())
            noteOff(heldNotes.get(0));
    }

    public void noteOff(int note, int velocity) {
        int lastNote = -1;
        if(!heldNotes.isEmpty())
            lastNote = heldNotes.get(heldNotes.size()-1);
        heldNotes.removeAll(List.of(new Integer[]{note}));
        List<Integer> newNotes = new ArrayList<>();
        for(Integer el : heldNotes)
            if(el != note)
                newNotes.add(el);
        heldNotes = newNotes;
        if(heldNotes.isEmpty())
            last.noteOff();
        else{
            int newNote = heldNotes.get(heldNotes.size()-1);
            if(lastNote != newNote)
                last.noteOn(newNote);
        }
        if (voiceByNote.containsKey(note)) {
            Voice voice = voiceByNote.remove(note);
            gatedVoices.remove(voice);
            releasedVoices.add(voice);
            voice.noteOff(velocity);
        }
    }

    public void noteOn(int note){
        noteOn(note, 64);
    }

    public void noteOff(int note){
        noteOff(note, 0);
    }

    public double getSample(int sampleId) {
        return soundSource.getSample(sampleId);
    }
}
