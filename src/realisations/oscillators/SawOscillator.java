package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractOscillator;

public class SawOscillator extends AbstractOscillator {
    public SawOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }
    public SawOscillator(SignalSource frequencySource, boolean randomPhase) {
        super(frequencySource, randomPhase);
    }

    @Override
    public double getSample(int sampleId) {
        return ((getPtr(sampleId) - 0.5) * 2);
    }
}
