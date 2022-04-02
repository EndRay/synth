package sources.oscillators;

import sources.AbstractSignalSource;
import sources.SignalSource;
import sources.utils.Socket;

abstract public class AbstractOscillator extends AbstractSignalSource implements Oscillator {
    private final Socket frequency = new Socket(), hardSync = new Socket();
    private double ptr;
    private boolean lastGate = false;
    double lastSample;

    public AbstractOscillator(){}
    public AbstractOscillator(double frequency){ frequency().set(frequency); }
    public AbstractOscillator(SignalSource frequencySource) {
        frequency().bind(frequencySource);
    }

    @Override
    public Socket frequency(){
        return frequency;
    }
    public Socket hardSync(){
        return hardSync;
    }

    public void setPtr(double ptr) {
        this.ptr = ptr;
    }

    /**
     * frequency < sampleRate
     */

    @Override
    protected boolean checkAndUpdateSampleId(int sampleId) {
        boolean res = super.checkAndUpdateSampleId(sampleId);
        if(res) {
            ptr += frequency().getFrequency(sampleId) / sampleRate;
            if (ptr < 0)
                ptr += 1;
            if (ptr >= 1)
                ptr -= 1;
        }
        return res;
    }

    public double getPtr(int sampleId) {
        checkAndUpdateSampleId(sampleId);
        return ptr;
    }

    abstract public double getAmplitude(int sampleId);

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId)) {
            boolean g = hardSync().getGate(sampleId);
            if(!lastGate && g)
                setPtr(0);
            lastGate = g;
            lastSample = getAmplitude(sampleId);
        }
        return lastSample;
    }

}
