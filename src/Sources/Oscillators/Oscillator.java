package Sources.Oscillators;

import Sources.SignalSource;

public interface Oscillator extends SignalSource {
    double getFrequency();
    void setFrequency(double frequency);
}
