package sources.filters;

import sources.SignalProcessor;
import sources.oscillators.Oscillator;
import sources.SignalSource;
import sources.utils.DC;

public interface Filter extends SignalProcessor, Oscillator {

    double getFrequency(int sampleId);

    void setFrequency(SignalSource frequencySource);

    double getSourceSample(int sampleId);
    void setSoundSource(SignalSource source);

    default void open() {
        setFrequency(DC.getFrequencyDC(maxFrequency));
    }
}
