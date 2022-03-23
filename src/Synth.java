import sources.SignalSource;

public interface Synth extends SignalSource {
    void noteOn(int note, int velocity);
    default void noteOn(int note){
        noteOn(note, 64);
    }
    void noteOff(int note, int velocity);
    default void noteOff(int note){
        noteOff(note, 0);
    }
}
