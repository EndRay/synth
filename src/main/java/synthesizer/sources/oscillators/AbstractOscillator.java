package synthesizer.sources.oscillators;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;

abstract public class AbstractOscillator extends AbstractSignalSource implements Oscillator {
    private final Socket frequency = new Socket(), hardSync = new Socket();
    private double ptr;
    private boolean lastGate = false;

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

    protected void setPtr(double ptr) {
        this.ptr = ptr;
    }

    protected abstract double getAmplitude(int sampleId);

    @Override
    protected double recalculate(int sampleId) {
        ptr += frequency().getFrequency(sampleId) / sampleRate;
        if (ptr < 0)
            ptr += 1;
        if (ptr >= 1)
            ptr -= 1;
        boolean g = hardSync().getGate(sampleId);
        if(!lastGate && g)
            setPtr(0);
        lastGate = g;
        return getAmplitude(sampleId);
    }

    /**
     * frequency < sampleRate
     */

    protected double getPtr(int sampleId) {
        return ptr;
    }
}
