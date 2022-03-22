package Sources.Utils;

import Sources.AbstractSoundSource;
import Sources.SignalSource;

public class Mixer extends AbstractSoundSource {

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
