package synthesizer.realisations.oscillators;

import synthesizer.realisations.oscillators.waveforms.SineWave;
import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.AbstractPMOscillator;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class PMSineOscillator extends AbstractPMOscillator {

    final static SineWave waveform = new SineWave();

    public PMSineOscillator() {
    }

    public PMSineOscillator(double frequency) {
        super(frequency);
    }

    public PMSineOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    public PMSineOscillator(SignalSource frequencySource, SignalSource phaseSource) {
        super(frequencySource, phaseSource);
    }

    @Override
    public double getAmplitude(int sampleId, double ptr) {
        return waveform.getAmplitude(sampleId, ptr);
    }
}
