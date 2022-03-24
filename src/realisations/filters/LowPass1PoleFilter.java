package realisations.filters;

import sources.SignalSource;
import sources.filters.Filter;

public class LowPass1PoleFilter extends AbstractSimpleLowPassFilter implements Filter {

    private double currentSample = 0;

    public LowPass1PoleFilter(){}

    public LowPass1PoleFilter(SignalSource source) {
        super(source);
    }

    public LowPass1PoleFilter(SignalSource source, SignalSource frequencySource) {
        super(source, frequencySource);
    }


    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            double f = getAlpha(sampleId);
            currentSample = currentSample + (source().getSample(sampleId) - currentSample) * f;
        }
        return currentSample;
    }
}
