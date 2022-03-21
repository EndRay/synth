package Sources.Oscillators;

public class SineOscillator extends AbstractOscillator {
    public SineOscillator(double frequency) {
        super(frequency);
    }

    @Override
    public double getSample(int sampleId) {
        nextSample(sampleId);
        return Math.sin((getPhase()) * 2 * Math.PI);
    }
}
