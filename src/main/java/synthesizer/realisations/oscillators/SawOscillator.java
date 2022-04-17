package synthesizer.realisations.oscillators;

import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.AbstractOscillator;

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

    @Override
    public double getAmplitude(int sampleId) {
        return ((0.5 - getPtr(sampleId)) * 2);
    }
}
