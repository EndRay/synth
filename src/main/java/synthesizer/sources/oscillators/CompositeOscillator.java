package synthesizer.sources.oscillators;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;
import synthesizer.sources.utils.SocketWithRequirement;

public class CompositeOscillator extends AbstractSignalSource {

    final private Socket waveScanner;
    final private SocketWithRequirement<Waveform> waveform;

    public CompositeOscillator(SignalSource waveScanner, Waveform waveform){
        this.waveScanner = new Socket(waveScanner);
        this.waveform = new SocketWithRequirement<>(waveform);
    }

    public Socket waveScanner(){
        return waveScanner;
    }
    public SocketWithRequirement<Waveform> waveform(){
        return waveform;
    }

    @Override
    protected double recalculate(int sampleId) {
        return waveform().getSource().getAmplitude(sampleId, waveScanner().getSample(sampleId));
    }
}
