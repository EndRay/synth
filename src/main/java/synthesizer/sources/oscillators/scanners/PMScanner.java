package synthesizer.sources.oscillators.scanners;

import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;

public class PMScanner extends SyncableScanner {

    final protected Socket phase = new Socket();

    public PMScanner(){}
    public PMScanner(double frequency){ super(frequency); }
    public PMScanner(SignalSource frequencySource){ super(frequencySource); }

    public Socket phase(){
        return phase;
    }

    @Override
    protected double recalculate(int sampleId) {
        super.recalculate(sampleId);
        double ptr = this.ptr;
        ptr += phase().getSample(sampleId);
        while (ptr < 0)
            ptr += 1;
        while (ptr >= 1)
            ptr -= 1;
        return ptr;
    }
}
