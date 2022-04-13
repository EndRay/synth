package synthesizer.sources.oscillators;

import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.Socket;

abstract public class AbstractPMOscillator extends AbstractOscillator implements PMOscillator {

    private final Socket phase = new Socket();


    public AbstractPMOscillator() {
    }

    public AbstractPMOscillator(double frequency) {
        super(frequency);
    }

    public AbstractPMOscillator(SignalSource frequencySource) {
        this(frequencySource, new DC(0));
    }

    public AbstractPMOscillator(SignalSource frequencySource, SignalSource phaseSource) {
        super(frequencySource);
        phase.bind(phaseSource);
    }

    @Override
    public Socket phase() {
        return phase;
    }

    public double getPtr(int sampleId) {
        double ptr = super.getPtr(sampleId);
        ptr += phase().getSample(sampleId);
        while (ptr < 0)
            ptr += 1;
        while (ptr >= 1)
            ptr -= 1;
        return ptr;
    }
}
