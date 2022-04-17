package synthesizer.realisations.oscillators;

import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.AbstractOscillator;
import synthesizer.sources.utils.Socket;

public class PulseOscillator extends AbstractOscillator {

    Socket pulseWidth = new Socket();


    public PulseOscillator(SignalSource frequencySource) {
        this(frequencySource, 0.5);
    }

    public PulseOscillator(SignalSource frequencySource, double pulseWidth) {
        super(frequencySource);
        this.pulseWidth.set(pulseWidth);
    }

    public PulseOscillator(SignalSource frequencySource, SignalSource pulseWidthSource) {
        super(frequencySource);
        pulseWidth.bind(pulseWidthSource);
    }

    public Socket pulseWidth(){
        return pulseWidth;
    }

    @Override
    public double getAmplitude(int sampleId) {
        return (getPtr(sampleId) < pulseWidth.getSample(sampleId) ? 1 : -1);
    }
}
