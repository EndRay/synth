package synthesizer.realisations.oscillators;

import synthesizer.realisations.oscillators.waveforms.TriangleWave;
import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.AbstractOscillator;

public class TriangleOscillator extends AbstractOscillator {

    final static TriangleWave waveform = new TriangleWave();

    public TriangleOscillator() {
    }

    public TriangleOscillator(double frequency) {
        super(frequency);
    }

    public TriangleOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    @Override
    public double getAmplitude(int sampleId, double ptr) {
        return waveform.getAmplitude(sampleId, ptr);
    }
}
