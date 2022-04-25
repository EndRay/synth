package synthesizer.realisations.oscillators.waveforms;

import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.AbstractWaveform;
import synthesizer.sources.utils.Socket;

public class PulseWave extends AbstractWaveform {

    final private Socket pulseWidth = new Socket();

    public PulseWave(){
        this.pulseWidth.set(0.5);
    }
    public PulseWave(double pulseWidth){
        this.pulseWidth.set(pulseWidth);
    }
    public PulseWave(SignalSource pulseWidth){
        this.pulseWidth.bind(pulseWidth);
    }

    public Socket pulseWidth(){
        return pulseWidth;
    }

    @Override
    public double getAmplitude(int sampleId, double ptr) {
        return (ptr < pulseWidth().getSample(sampleId) ? 1 : -1);
    }
}
