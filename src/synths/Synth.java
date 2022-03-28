package synths;

import sources.SignalSource;
import sources.utils.SourceValue;

public interface Synth extends SignalSource {
    void noteOn(int note, int velocity);
    default void noteOn(int note){
        noteOn(note, 64);
    }
    void noteOff(int note, int velocity);
    default void noteOff(int note){
        noteOff(note, 0);
    }
    void midiCC(int CC, int value);
    void addToMap(SourceValue value);
    void startMapping();
    void stopMapping();
}
