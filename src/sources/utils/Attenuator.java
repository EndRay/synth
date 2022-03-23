package sources.utils;

import sources.AbstractSignalProcessor;
import sources.AbstractSignalSource;
import sources.SignalSource;

public class Attenuator extends AbstractSignalProcessor {

    SignalSource coefficientSource;
    double lastSample;

    public Attenuator(SignalSource source, SignalSource coefficientSource){
        super(source);
        this.coefficientSource = coefficientSource;
    }

    public void setCoefficient(SignalSource coefficientSource){
        this.coefficientSource = coefficientSource;
    }
    public double getCoefficient(int sampleId){
        return coefficientSource.getSample(sampleId);
    }

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId))
            lastSample = getSourceSample(sampleId) * getCoefficient(sampleId);
        return lastSample;
    }
}
