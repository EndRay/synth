package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractOscillator;

/**
 * ramp-down saw
 */

public class SawOscillator extends AbstractOscillator {
    public SawOscillator() {
    }

    public SawOscillator(double frequency) {
        super(frequency);
    }

    public SawOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    public SawOscillator(SignalSource frequencySource, boolean randomPhase) {
        super(frequencySource, randomPhase);
    }

    @Override
    public double getSample(int sampleId) {
        return ((0.5 - getPtr(sampleId)) * 2);
    }
}
