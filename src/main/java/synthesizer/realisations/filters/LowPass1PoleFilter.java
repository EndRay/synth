package synthesizer.realisations.filters;

import synthesizer.sources.SignalSource;
import synthesizer.sources.filters.Filter;

public class LowPass1PoleFilter extends AbstractSimpleLowPassFilter implements Filter {

    private double currentSample = 0;

    public LowPass1PoleFilter(){}

    public LowPass1PoleFilter(double frequency){
        super(frequency);
    }

    public LowPass1PoleFilter(SignalSource source) {
        super(source);
    }

    public LowPass1PoleFilter(SignalSource source, SignalSource frequencySource) {
        super(source, frequencySource);
    }


    @Override
    protected double recalculate(int sampleId) {
        double f = getAlpha(sampleId);
        currentSample = currentSample + (source().getSample(sampleId) - currentSample) * f;
        return currentSample;
    }
}
