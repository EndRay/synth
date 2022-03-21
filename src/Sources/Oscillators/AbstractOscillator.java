package Sources.Oscillators;

import Sources.AbstractSoundSource;

abstract public class AbstractOscillator extends AbstractSoundSource implements Oscillator {
    double frequency;
    double phase;

    AbstractOscillator(double frequency) {
        this.frequency = frequency;
        phase = 0;
    }

    public double getPhase() {
        return phase;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setPhase(double phase) {
        this.phase = phase;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public void hardSync() {
        phase = 0;
    }

    /**
     * frequency < sampleRate
     */
    void nextSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId)) {
            phase += frequency / sampleRate;
            if (phase < 0)
                phase += 1;
            if (phase >= 1)
                phase -= 1;
        }
    }
}
