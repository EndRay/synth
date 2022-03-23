package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractSimpleOscillator;

public class SineOscillator extends AbstractSimpleOscillator {
    public SineOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }
    public SineOscillator(SignalSource frequencySource, boolean randomPhase) {
        super(frequencySource, randomPhase);
    }

    @Override
    public double getSample(int sampleId) {
        nextSample(sampleId);
        return Math.sin((getPhase()) * 2 * Math.PI);
    }
}
