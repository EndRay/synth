package Sources.Utils;

import Sources.AbstractSoundSource;
import Sources.SignalSource;

public class Offset extends AbstractSoundSource {

    SignalSource source;
    double offset;
    double lastSample;

    public Offset(SignalSource source, double offset){
        this.source = source;
        this.offset = offset;
    }

    public void setOffset(double offset){
        this.offset = offset;
    }

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId))
            lastSample = source.getSample(sampleId) + offset;
        return lastSample;
    }
}
