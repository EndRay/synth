package synthesizer.sources.utils;

import synthesizer.sources.AbstractSignalProcessor;
import synthesizer.sources.SignalSource;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Clip extends AbstractSignalProcessor {

    boolean bipolar;

    public Clip(SignalSource source){
        this(source, false);
    }

    public Clip(SignalSource source, boolean bipolar){
        super(source);
        this.bipolar = bipolar;
    }

    @Override
    protected double recalculate(int sampleId) {
        double res = source().getSample(sampleId);
        res = min(res, 1);
        if(bipolar) res = max(res, -1);
        else res = max(res, 0);
        return res;
    }
}
