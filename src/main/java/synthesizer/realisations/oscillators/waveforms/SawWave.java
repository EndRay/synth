package synthesizer.realisations.oscillators.waveforms;

import synthesizer.sources.oscillators.AbstractWaveform;

public class SawWave extends AbstractWaveform {

    public SawWave(){}

    @Override
    public double getAmplitude(int sampleId, double ptr) {
        return ((0.5 - ptr) * 2);
    }
}
