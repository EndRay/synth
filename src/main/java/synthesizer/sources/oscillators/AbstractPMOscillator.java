package synthesizer.sources.oscillators;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.scanners.PMScanner;
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.Socket;

abstract public class AbstractPMOscillator extends AbstractSignalSource implements PMOscillator, Waveform {

    final protected PMScanner waveScanner = new PMScanner();

    public AbstractPMOscillator() {
    }

    public AbstractPMOscillator(double frequency) {
        frequency().set(frequency);
    }

    public AbstractPMOscillator(SignalSource frequencySource) {
        frequency().bind(frequencySource);
    }

    public AbstractPMOscillator(SignalSource frequencySource, SignalSource phaseSource) {
        frequency().bind(frequencySource);
        phase().bind(phaseSource);
    }
    @Override
    public Socket frequency(){
        return waveScanner.frequency();
    }
    @Override
    public Socket phase() {
        return waveScanner.phase();
    }
    public Socket hardSync(){
        return waveScanner.hardSync();
    }

    /**
     * frequency < sampleRate
     */
    @Override
    protected double recalculate(int sampleId) {
        return getAmplitude(sampleId, waveScanner.getSample(sampleId));
    }

}
