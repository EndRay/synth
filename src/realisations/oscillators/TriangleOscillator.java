package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractOscillator;

public class TriangleOscillator extends AbstractOscillator {
    public TriangleOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }
    public TriangleOscillator(SignalSource frequencySource, boolean randomPhase) {
        super(frequencySource, randomPhase);
    }

    @Override
    public double getSample(int sampleId) {
        double p = getPtr(sampleId);
        return (p < 0.5 ? 4*p-1 : 3-4*p);
    }
}
