package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractOscillator;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class SineOscillator extends AbstractOscillator {
    public SineOscillator() {
    }

    public SineOscillator(double frequency) {
        super(frequency);
    }

    public SineOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    @Override
    public double getAmplitude(int sampleId) {
        return sin(getPtr(sampleId) * 2 * PI);
    }
}
