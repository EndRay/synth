package synthesizer.sources.oscillators;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;
import synthesizer.sources.utils.SocketWithRequirement;

public class CompositeOscillator extends AbstractSignalSource {

    final private Socket wavescanner;
    final private SocketWithRequirement<Waveform> waveform;

    public CompositeOscillator(SignalSource wavescanner, Waveform waveform){
        this.wavescanner = new Socket(wavescanner);
        this.waveform = new SocketWithRequirement<>(waveform);
    }

    public Socket wavescanner(){
        return wavescanner;
    }
    public SocketWithRequirement<Waveform> waveform(){
        return waveform;
    }

    @Override
    protected double recalculate(int sampleId) {
        return waveform().getSource().getAmplitude(sampleId, wavescanner().getSample(sampleId));
    }
}
