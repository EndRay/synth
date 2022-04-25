package synthesizer.realisations.oscillators;

import synthesizer.realisations.oscillators.waveforms.SawWave;
import synthesizer.realisations.oscillators.waveforms.SineWave;
import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.AbstractOscillator;
import synthesizer.sources.oscillators.Waveform;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class SineOscillator extends AbstractOscillator {

    final static SineWave waveform = new SineWave();

    public SineOscillator() {
    }

    public SineOscillator(double frequency) {
        super(frequency);
    }

    public SineOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    @Override
    public double getAmplitude(int sampleId, double ptr) {
        return waveform.getAmplitude(sampleId, ptr);
    }
}
