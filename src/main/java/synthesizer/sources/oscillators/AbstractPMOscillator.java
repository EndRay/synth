package synthesizer.sources.oscillators;

import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.scanners.PMScanner;
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.Socket;

abstract public class AbstractPMOscillator extends AbstractOscillator implements PMOscillator {

    final protected PMScanner wavescanner = new PMScanner();

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
        phase().bind(phaseSource);
    }

    @Override
    public Socket phase() {
        return wavescanner.phase();
    }
}
