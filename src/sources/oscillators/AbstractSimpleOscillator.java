package sources.oscillators;

import sources.AbstractSignalSource;
import sources.SignalSource;

import java.util.Random;

abstract public class AbstractSimpleOscillator extends AbstractSignalSource implements Oscillator {
    SignalSource frequencySource;
    Random rd = new Random();
    double phase;

    public AbstractSimpleOscillator(SignalSource frequencySource) {
        this(frequencySource, false);
    }
    public AbstractSimpleOscillator(SignalSource frequencySource, boolean randomPhase) {
        this.frequencySource = frequencySource;
        phase = randomPhase ? rd.nextDouble() : 0;
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
         setPhase(0);
    }

    /**
     * frequency < sampleRate
     */
    protected void nextSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            phase += getFrequency(sampleId) / sampleRate;
            if (phase < 0)
                phase += 1;
            if (phase >= 1)
                phase -= 1;
        }
    }
}
