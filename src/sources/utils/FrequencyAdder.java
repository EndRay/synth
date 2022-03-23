package sources.utils;

import sources.AbstractSignalSource;
import sources.SignalSource;

public class FrequencyAdder extends AbstractSignalSource {

    SignalSource[] sources;
    double lastSample;

    public FrequencyAdder(SignalSource... sources) {
        this.sources = sources;
    }

    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            lastSample = 0;
            for (SignalSource source : sources)
                lastSample += SignalSource.voltageToFrequency(source.getSample(sampleId));
            lastSample = SignalSource.frequencyToVoltage(lastSample);
        }
        return lastSample;
    }
}
