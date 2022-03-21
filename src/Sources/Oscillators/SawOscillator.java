package Sources.Oscillators;

public class SawOscillator extends AbstractOscillator {
    public SawOscillator(double frequency) {
        super(frequency);
    }

    @Override
    public double getSample(int sampleId) {
        nextSample(sampleId);
        return ((getPhase() - 0.5) * 2);
    }
}
