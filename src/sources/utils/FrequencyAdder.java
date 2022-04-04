package sources.utils;

import sources.AbstractSignalSource;
import sources.SignalSource;

public class FrequencyAdder extends AbstractSignalSource {

    SignalSource source;
    double frequency;

    public FrequencyAdder(SignalSource source, double frequency) {
        this.source = source;
        this.frequency = frequency;
    }

    @Override
    protected double recalculate(int sampleId) {
        return SignalSource.frequencyToVoltage(SignalSource.voltageToFrequency(source.getSample(sampleId)) + frequency);
    }
}
