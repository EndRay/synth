package synthesizer.realisations.oscillators;

import synthesizer.realisations.oscillators.waveforms.PulseWave;
import synthesizer.sources.SignalSource;
import synthesizer.sources.oscillators.AbstractOscillator;
import synthesizer.sources.utils.Socket;

public class PulseOscillator extends AbstractOscillator {

    final PulseWave waveform = new PulseWave();

    public PulseOscillator(SignalSource frequencySource) {
        this(frequencySource, 0.5);
    }

    public PulseOscillator(SignalSource frequencySource, double pulseWidth) {
        super(frequencySource);
        pulseWidth().set(pulseWidth);
    }

    public PulseOscillator(SignalSource frequencySource, SignalSource pulseWidthSource) {
        super(frequencySource);
        pulseWidth().bind(pulseWidthSource);
    }

    public Socket pulseWidth(){
        return waveform.pulseWidth();
    }

    @Override
    public double getAmplitude(int sampleId, double ptr) {
        return waveform.getAmplitude(sampleId, ptr);
    }
}
