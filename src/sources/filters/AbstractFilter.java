package sources.filters;

import sources.AbstractSignalProcessor;
import sources.SignalSource;

abstract public class AbstractFilter extends AbstractSignalProcessor implements Filter {
    private SignalSource frequencySource;

    public AbstractFilter(SignalSource source) {
        super(source);
        open();
    }

    public AbstractFilter(SignalSource source, SignalSource frequencySource) {
        this(source);
        setFrequency(frequencySource);
    }

    @Override
    public double getFrequency(int sampleId) {
        return SignalSource.voltageToFrequency(frequencySource.getSample(sampleId));
    }

    @Override
    public void setFrequency(SignalSource frequencySource) {
        this.frequencySource = frequencySource;
    }

    public double getAlpha(int sampleId) {
        double tmp = 2 * Math.PI * samplingPeriod * getFrequency(sampleId);
        return tmp / (tmp + 1);
    }

    abstract public double getSample(int sampleId);
}
