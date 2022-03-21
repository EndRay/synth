package Sources.Utils;

import Sources.AbstractSoundSource;
import Sources.SignalSource;

public class Attenuator extends AbstractSoundSource {

    SignalSource source;
    double coefficient;
    double lastSample;

    public Attenuator(SignalSource source, double coefficient){
        this.source = source;
        this.coefficient = coefficient;
    }

    public void setCoefficient(double coefficient){
        this.coefficient = coefficient;
    }

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId))
            lastSample = source.getSample(sampleId) * coefficient;
        return lastSample;
    }
}
