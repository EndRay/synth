package synthesizer.sources.oscillators;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.scanners.SyncableScanner;
import synthesizer.sources.utils.Socket;

abstract public class AbstractOscillator extends AbstractSignalSource implements Oscillator, Waveform {
    final protected SyncableScanner wavescanner = new SyncableScanner();
    private double ptr;
    private boolean lastGate = false;

    public AbstractOscillator(){ frequency().set(SignalSource.tuningFrequency); }
    public AbstractOscillator(double frequency){ frequency().set(frequency); }
    public AbstractOscillator(SignalSource frequencySource) {
        frequency().bind(frequencySource);
    }

    @Override
    public Socket frequency(){
        return wavescanner.frequency();
    }
    public Socket hardSync(){
        return wavescanner.hardSync();
    }

    /**
     * frequency < sampleRate
     */
    @Override
    protected double recalculate(int sampleId) {
        ptr += frequency().getFrequency(sampleId) / sampleRate;
        if (ptr < 0)
            ptr += 1;
        if (ptr >= 1)
            ptr -= 1;
        boolean g = hardSync().getGate(sampleId);
        if(!lastGate && g)
            ptr = 0;
        lastGate = g;
        return getAmplitude(sampleId, ptr);
    }
}
