package sources.utils;

import sources.AbstractSignalProcessor;
import sources.AbstractSignalSource;
import sources.SignalSource;

public class Attenuator extends AbstractSignalProcessor {

    private final Socket coefficient = new Socket();
    double lastSample;

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
            lastSample = getSourceSample(sampleId) * coefficient().getSample(sampleId);
        return lastSample;
    }
}
