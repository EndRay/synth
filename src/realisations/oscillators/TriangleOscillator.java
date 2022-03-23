package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractSimpleOscillator;

public class TriangleOscillator extends AbstractSimpleOscillator {
    public TriangleOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }
    public TriangleOscillator(SignalSource frequencySource, boolean randomPhase) {
        super(frequencySource, randomPhase);
    }

    @Override
    public double getSample(int sampleId) {
        nextSample(sampleId);
        double p = getPhase();
        return (p < 0.5 ? 4*p-1 : 3-4*p);
    }
}
