package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractOscillator;
import sources.utils.Socket;
import sources.utils.SourceValue;

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
