package Sources.Filters;

import Sources.AbstractSoundSource;
import Sources.SignalSource;

abstract public class AbstractFilter extends AbstractSoundSource implements Filter{
    final SignalSource source;
    double currentSample;
    double cutoff;

    AbstractFilter(SignalSource source){
        this.source = source;
        currentSample = 0;
        open();
    }
    AbstractFilter(SignalSource source, double frequency){
        this(source);
        setFrequency(frequency);
    }

    @Override
    public double getFrequency() {
        return cutoff;
    }

    @Override
    public void setFrequency(double frequency) {
        cutoff = frequency;
    }

    public double getAlpha(){
        double tmp = 2*Math.PI * samplingPeriod * cutoff;
        return tmp / (tmp+1);
    }

    abstract public double getSample(int sampleId);
}
