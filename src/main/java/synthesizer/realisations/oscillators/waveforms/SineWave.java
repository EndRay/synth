package synthesizer.realisations.oscillators.waveforms;

import synthesizer.realisations.oscillators.SineOscillator;
import synthesizer.sources.oscillators.AbstractWaveform;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class SineWave extends AbstractWaveform {

    public SineWave(){}

    @Override
    public double getAmplitude(int sampleId, double ptr) {
        return sin(ptr * 2 * PI);
    }
}
