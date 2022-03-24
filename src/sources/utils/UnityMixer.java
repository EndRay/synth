package sources.utils;

import sources.SignalSource;

public class UnityMixer extends Mixer{

    public UnityMixer(SignalSource... sources) {
        super(sources);
    }

    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            lastSample = 0;
            for(int i = 0; i < size(); ++i)
                lastSample += get(i).getSample(sampleId);
            lastSample /= size();
        }
        return lastSample;
    }
}
