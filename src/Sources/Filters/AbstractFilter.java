package Sources.Filters;

import Sources.AbstractSoundSource;
import Sources.SignalSource;

abstract public class AbstractFilter extends AbstractSoundSource implements Filter {
    SignalSource source;
    SignalSource frequencySource;
    double currentSample;

    AbstractFilter(SignalSource source) {
        this.source = source;
        currentSample = 0;
        open();
    }

    AbstractFilter(SignalSource source, SignalSource frequencySource) {
        this(source);
        setFrequency(frequencySource);
    }

    @Override
    public void setSoundSource(SignalSource source) {
        this.source = source;
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
