package Sources.Oscillators;

import Sources.SignalSource;

public interface Oscillator extends SignalSource {
    double getFrequency(int sampleId);

    void setFrequency(SignalSource frequencySource);
}
