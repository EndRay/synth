package sources.oscillators;

import sources.SignalSource;

public interface Oscillator extends SignalSource {
    double getFrequency(int sampleId);

    void setFrequency(SignalSource frequencySource);
}
