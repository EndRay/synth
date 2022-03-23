package sources.oscillators;

import sources.AbstractSignalSource;
import sources.SignalSource;

import java.util.Random;

abstract public class AbstractOscillator extends AbstractSignalSource implements Oscillator {
    SignalSource frequencySource;
    Random rd = new Random();
    private double ptr;

    public AbstractOscillator(SignalSource frequencySource) {
        this(frequencySource, false);
    }
    public AbstractOscillator(SignalSource frequencySource, boolean randomPhase) {
        this.frequencySource = frequencySource;
        ptr = randomPhase ? rd.nextDouble() : 0;
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
         ptr = 0;
    }

    /**
     * frequency < sampleRate
     */
    public double getPtr(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            ptr += getFrequency(sampleId) / sampleRate;
            if (ptr < 0)
                ptr += 1;
            if (ptr >= 1)
                ptr -= 1;
        }
        return ptr;
    }
}
