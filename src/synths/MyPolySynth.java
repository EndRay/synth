package synths;

import sources.SignalSource;
import sources.utils.SourceValue;
import sources.voices.Voice;

import java.util.*;

/**
 * TODO: Correct last note
 */

public class MyPolySynth implements Synth {
    Voice[] voices;
    Map<Integer, Voice> voiceByNote = new HashMap<>();
    Set<Voice> freeVoices = new LinkedHashSet<>(), releasedVoices = new LinkedHashSet<>(), gatedVoices = new LinkedHashSet<>();
    SignalSource soundSource;
    LinkedList<SourceValue> valuesToMap = new LinkedList<>();
    Map<Integer, SourceValue> valueByCC = new HashMap<>();
    boolean nowMapping = false;

    List<Integer> heldNotes = new ArrayList<>();
    Voice last;

    Set<Integer> forbiddenCCs = new HashSet<>();
    {
        for(int i = 32; i <= 63; ++i)
            forbiddenCCs.add(i);
        for(int i = 120; i <= 127; ++i)
            forbiddenCCs.add(i);
    }

    public MyPolySynth(Voice[] voices, SignalSource soundSource, Voice last) {
        this.voices = voices;
        this.soundSource = soundSource;
        this.last = last;
        freeVoices.addAll(Arrays.asList(voices));
    }

    private void tryToMap(){
        if(!nowMapping || valuesToMap.isEmpty())
            return;
        System.out.println("Mapping \"" + valuesToMap.getFirst().getDescription() + "\"");
    }

    public void addToMap(SourceValue value){
        valuesToMap.add(value);
    }

    public void startMapping(){
        nowMapping = true;
        tryToMap();
    }

    public void stopMapping(){
        nowMapping = false;
    }

    @Override
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

    @Override
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

    @Override
    public void midiCC(int CC, int value) {
        if (!valueByCC.containsKey(CC)) {
            if (!nowMapping || valuesToMap.isEmpty() || forbiddenCCs.contains(CC))
                return;
            valueByCC.put(CC, valuesToMap.removeFirst());
            System.out.println("\"" + valueByCC.get(CC).getDescription() + "\" mapped to CC" + CC);
            tryToMap();
        }
        valueByCC.get(CC).setValue(value / 128.0);
    }


    @Override
    public double getSample(int sampleId) {
        return soundSource.getSample(sampleId);
    }
}
