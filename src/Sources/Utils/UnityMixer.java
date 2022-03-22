package Sources.Utils;

import Sources.SignalSource;

public class UnityMixer extends Mixer{

    public UnityMixer(SignalSource... sources) {
        super(sources);
    }

    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            lastSample = 0;
            for (SignalSource source : sources)
                lastSample += source.getSample(sampleId);
            lastSample /= sources.length;
        }
        return lastSample;
    }
}
