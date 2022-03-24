package sources.utils;

import sources.AbstractSignalSource;
import sources.SignalSource;

public class FrequencyAdder extends AbstractSignalSource {

    SignalSource source;
    double frequency;
    double lastSample;

    public FrequencyAdder(SignalSource source, double frequency) {
        this.source = source;
        this.frequency = frequency;
    }

    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId))
            lastSample = SignalSource.frequencyToVoltage(SignalSource.voltageToFrequency(source.getSample(sampleId)) + frequency);
        return lastSample;
    }
}
