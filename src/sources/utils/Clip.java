package sources.utils;

import sources.AbstractSignalProcessor;
import sources.SignalSource;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Clip extends AbstractSignalProcessor {

    double lastSample;
    boolean bipolar;

    public Clip(SignalSource source){
        this(source, false);
    }

    public Clip(SignalSource source, boolean bipolar){
        super(source);
        this.bipolar = bipolar;
    }

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId)){
            lastSample = source().getSample(sampleId);
            lastSample = min(lastSample, 1);
            if(bipolar) lastSample = max(lastSample, -1);
            else lastSample = max(lastSample, 0);
        }
        return lastSample;
    }
}
