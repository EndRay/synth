package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractSimpleOscillator;

public class SawOscillator extends AbstractSimpleOscillator {
    public SawOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }
    public SawOscillator(SignalSource frequencySource, boolean randomPhase) {
        super(frequencySource, randomPhase);
    }

    @Override
    public double getSample(int sampleId) {
        nextSample(sampleId);
        return ((getPhase() - 0.5) * 2);
    }
}
