package synthesizer.realisations.oscillators.waveforms;

import synthesizer.sources.oscillators.AbstractWaveform;

public class TriangleWave extends AbstractWaveform {

    public TriangleWave(){}

    @Override
    public double getAmplitude(int sampleId, double ptr) {
        return (ptr < 0.5 ? 4*ptr-1 : 3-4*ptr);
    }
}
