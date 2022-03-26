package sources.oscillators;

import sources.AbstractSignalSource;
import sources.SignalSource;
import sources.utils.Socket;

import java.util.Random;

abstract public class AbstractOscillator extends AbstractSignalSource implements Oscillator {
    private final Socket frequency = new Socket();
    protected Random rd = new Random();
    private double ptr;

    public AbstractOscillator(){}
    public AbstractOscillator(double frequency){ frequency().set(frequency); }
    public AbstractOscillator(SignalSource frequencySource) {
        this(frequencySource, false);
    }
    public AbstractOscillator(SignalSource frequencySource, boolean randomPhase) {
        frequency().bind(frequencySource);
        ptr = randomPhase ? rd.nextDouble() : 0;
    }

    @Override
    public Socket frequency(){
        return frequency;
    }

    public void hardSync() {
         ptr = 0;
    }

    /**
     * frequency < sampleRate
     */
    public double getPtr(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            ptr += frequency().getFrequency(sampleId) / sampleRate;
            if (ptr < 0)
                ptr += 1;
            if (ptr >= 1)
                ptr -= 1;
        }
        return ptr;
    }
}
