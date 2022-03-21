package Sources.Filters;

import Sources.Oscillators.Oscillator;
import Sources.SignalSource;
import Sources.Utils.DC;

public interface Filter extends SignalSource, Oscillator {
    double getFrequency(int sampleId);
    void setFrequency(SignalSource frequencySource);
    default void open(){
        setFrequency(new DC(maxFrequency));
    }
}
