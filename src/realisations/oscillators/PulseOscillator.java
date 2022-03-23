package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractOscillator;
import sources.utils.DC;

public class PulseOscillator extends AbstractOscillator {

    SignalSource pulseWidthSource;

    public PulseOscillator(SignalSource frequencySource) {
        this(frequencySource, 0.5, false);
    }

    public PulseOscillator(SignalSource frequencySource, boolean randomPhase) {
        this(frequencySource, 0.5, randomPhase);
    }

    public PulseOscillator(SignalSource frequencySource, double pulseWidth) {
        this(frequencySource, new DC(pulseWidth), false);
    }

    public PulseOscillator(SignalSource frequencySource, double pulseWidth, boolean randomPhase) {
        this(frequencySource, new DC(pulseWidth), false);
    }

    public PulseOscillator(SignalSource frequencySource, SignalSource pulseWidthSource) {
        this(frequencySource, pulseWidthSource, false);
    }

    public PulseOscillator(SignalSource frequencySource, SignalSource pulseWidthSource, boolean randomPhase) {
        super(frequencySource, randomPhase);
        this.pulseWidthSource = pulseWidthSource;
    }

    public void setPulseWidth(SignalSource pulseWidthSource) {
        this.pulseWidthSource = pulseWidthSource;
    }

    public double getPulseWidth(int sampleId) {
        return pulseWidthSource.getSample(sampleId);
    }

    @Override
    public double getSample(int sampleId) {
        return (getPtr(sampleId) < getPulseWidth(sampleId) ? 1 : -1);
    }
}
