import sources.SignalSource;

public interface Synth extends SignalSource {
    void noteOn(int note, double velocity);
    default void noteOn(int note){
        noteOn(note, 0.5);
    }
    void noteOff(int note, double velocity);
    default void noteOff(int note){
        noteOff(note, 0);
    }
}
