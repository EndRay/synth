package synthesizer.realisations.oscillators;

import synthesizer.realisations.oscillators.waveforms.SawWave;
import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.AbstractOscillator;
import synthesizer.sources.oscillators.Waveform;

/**
 * ramp-down saw
 */

public class SawOscillator extends AbstractOscillator {

    final static SawWave waveform = new SawWave();

    public SawOscillator() {
    }

    public SawOscillator(double frequency) {
        super(frequency);
    }

    public SawOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    @Override
    public double getAmplitude(int sampleId, double ptr) {
        return waveform.getAmplitude(sampleId, ptr);
    }
}
