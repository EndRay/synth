package sources.utils;

import sources.AbstractSignalSource;
import sources.SignalSource;

public class Mixer extends AbstractSignalSource {

    SignalSource[] sources;
    double lastSample;

    public Mixer(SignalSource... sources) {
        this.sources = sources;
    }

    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            lastSample = 0;
            for (SignalSource source : sources)
                lastSample += source.getSample(sampleId);
        }
        return lastSample;
    }
}
