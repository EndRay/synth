package Sources.Oscillators;

import Sources.AbstractSoundSource;
import Sources.SignalSource;

abstract public class AbstractOscillator extends AbstractSoundSource implements Oscillator {
    SignalSource frequencySource;
    double phase;

    AbstractOscillator(SignalSource frequencySource) {
        this.frequencySource = frequencySource;
        phase = 0;
    }

    public double getPhase() {
        return phase;
    }

    public void setPhase(double phase) {
        this.phase = phase;
    }

    @Override
    public double getFrequency(int sampleId) {
        return SignalSource.voltageToFrequency(frequencySource.getSample(sampleId));
    }

    @Override
    public void setFrequency(SignalSource frequencySource) {
        this.frequencySource = frequencySource;
    }

    public void hardSync() {
        phase = 0;
    }

    /**
     * frequency < sampleRate
     */
    void nextSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId)) {
            phase += getFrequency(sampleId) / sampleRate;
            if (phase < 0)
                phase += 1;
            if (phase >= 1)
                phase -= 1;
        }
    }
}
