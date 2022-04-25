package synthesizer.sources.oscillators;

import synthesizer.sources.AbstractSignalSource;

public abstract class AbstractWaveform extends AbstractSignalSource implements Waveform {
    private double ptr = 0;
    @Override
    protected double recalculate(int sampleId) {
        ptr += 1 / tuningFrequency / sampleRate;
        if(ptr >= 1)
            ptr -= 1;
        return getAmplitude(sampleId, ptr);
    }
}
