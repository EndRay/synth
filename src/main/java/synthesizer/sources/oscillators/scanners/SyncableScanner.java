package synthesizer.sources.oscillators.scanners;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;

public class SyncableScanner extends AbstractSignalSource {

    final protected Socket frequency = new Socket(), hardSync = new Socket();
    protected double ptr;
    protected boolean lastGate = false;

    public SyncableScanner(){ frequency.setFrequency(SignalSource.tuningFrequency); }
    public SyncableScanner(double frequency){ frequency().set(frequency); }
    public SyncableScanner(SignalSource frequencySource) {
        frequency().bind(frequencySource);
    }

    public Socket frequency(){
        return frequency;
    }
    public Socket hardSync(){
        return hardSync;
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
        return ptr;
    }
}
