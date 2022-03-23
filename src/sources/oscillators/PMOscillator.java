package sources.oscillators;

import sources.SignalSource;

public interface PMOscillator extends Oscillator{
    double getPhase(int sampleId);
    void setPhase(SignalSource phaseSource);
}
