package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractPMOscillator;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class PMSineOscillator extends AbstractPMOscillator {
    public PMSineOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    public PMSineOscillator(SignalSource frequencySource, SignalSource phaseSource) {
        super(frequencySource, phaseSource);
    }

    @Override
    public double getSample(int sampleId) {
        return sin(getPtr(sampleId) * 2 * PI);
    }
}
