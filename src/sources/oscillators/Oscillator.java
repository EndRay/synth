package sources.oscillators;

import sources.SignalSource;

/**
 * -1 <= getSample(id) <= 1
 */

public interface Oscillator extends SignalSource {
    double getFrequency(int sampleId);

    void setFrequency(SignalSource frequencySource);
}
