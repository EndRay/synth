package sources.filters;

import sources.oscillators.Oscillator;
import sources.SignalSource;
import sources.utils.DC;

public interface Filter extends SignalSource, Oscillator {
    void setSoundSource(SignalSource source);

    double getFrequency(int sampleId);

    void setFrequency(SignalSource frequencySource);

    default void open() {
        setFrequency(new DC(maxFrequency));
    }
}
