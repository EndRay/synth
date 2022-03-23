package sources.filters;

import sources.SignalSource;

public class LowPass1PoleFilter extends AbstractFilter implements Filter {
    public LowPass1PoleFilter(SignalSource source) {
        super(source);
    }

    public LowPass1PoleFilter(SignalSource source, SignalSource frequencySource) {
        super(source, frequencySource);
    }

    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            source.getSample(sampleId);
            double f = getAlpha(sampleId);
            currentSample = currentSample + (source.getSample(sampleId) - currentSample) * f;
        }
        return currentSample;
    }
}
