package Sources.Oscillators;

import Sources.SignalSource;

public class SawOscillator extends AbstractOscillator {
    public SawOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    @Override
    public double getSample(int sampleId) {
        nextSample(sampleId);
        return ((getPhase() - 0.5) * 2);
    }
}
