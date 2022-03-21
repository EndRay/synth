package Sources.Filters;

import Sources.SignalSource;

public class LowPass1PoleFilter extends AbstractFilter implements Filter{
    public LowPass1PoleFilter(SignalSource source){
        super(source);
    }
    public LowPass1PoleFilter(SignalSource source, double frequency){
        super(source, frequency);
    }

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId)) {
            source.getSample(sampleId);
            double f = getAlpha();
            currentSample = currentSample + (source.getSample(sampleId) - currentSample) * f;
        }
        return currentSample;
    }
}
