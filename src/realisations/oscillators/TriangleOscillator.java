package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractOscillator;

public class TriangleOscillator extends AbstractOscillator {
    public TriangleOscillator() {
    }

    public TriangleOscillator(double frequency) {
        super(frequency);
    }

    public TriangleOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    @Override
    public double getAmplitude(int sampleId) {
        double ptr = getPtr(sampleId);
        return (ptr < 0.5 ? 4*ptr-1 : 3-4*ptr);
    }
}
