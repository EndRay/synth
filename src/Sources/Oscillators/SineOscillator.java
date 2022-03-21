package Sources.Oscillators;

import Sources.SignalSource;

public class SineOscillator extends AbstractOscillator {
    public SineOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    @Override
    public double getSample(int sampleId) {
        nextSample(sampleId);
        return Math.sin((getPhase()) * 2 * Math.PI);
    }
}
