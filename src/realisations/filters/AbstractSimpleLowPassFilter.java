package realisations.filters;

import sources.AbstractSignalSource;
import sources.SignalSource;
import sources.filters.AbstractFilter;

abstract public class AbstractSimpleLowPassFilter extends AbstractFilter {
    public AbstractSimpleLowPassFilter() {}

    public AbstractSimpleLowPassFilter(SignalSource source) { super(source); }

    public AbstractSimpleLowPassFilter(SignalSource source, SignalSource frequencySource) {
        super(source, frequencySource);
    }

    protected double getAlpha(int sampleId) {
        double tmp = 2 * Math.PI * samplingPeriod * frequency().getFrequency(sampleId);
        return tmp / (tmp + 1);
    }
}
