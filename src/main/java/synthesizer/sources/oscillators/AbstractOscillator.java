package synthesizer.sources.oscillators;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.scanners.SyncableScanner;
import synthesizer.sources.utils.Socket;

abstract public class AbstractOscillator extends AbstractSignalSource implements Oscillator, Waveform {
    final protected SyncableScanner waveScanner = new SyncableScanner();

    public AbstractOscillator(){ frequency().set(SignalSource.tuningFrequency); }
    public AbstractOscillator(double frequency){ frequency().set(frequency); }
    public AbstractOscillator(SignalSource frequencySource) {
        frequency().bind(frequencySource);
    }

    @Override
    public Socket frequency(){
        return waveScanner.frequency();
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
