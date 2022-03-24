package sources.utils;

import sources.AbstractSignalProcessor;
import sources.AbstractSignalSource;
import sources.SignalSource;

public class Attenuator extends AbstractSignalProcessor {

    private final Socket coefficient = new Socket();
    double lastSample;

    public Attenuator(double coefficient){
        coefficient().set(coefficient);
    }

    public Attenuator(SignalSource coefficientSource){
        coefficient().bind(coefficientSource);
    }

    public Attenuator(SignalSource source, SignalSource coefficientSource){
        super(source);
        coefficient().bind(coefficientSource);
    }

    public Socket coefficient(){
        return coefficient;
    }

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId))
            lastSample = source().getSample(sampleId) * coefficient().getSample(sampleId);
        return lastSample;
    }
}
