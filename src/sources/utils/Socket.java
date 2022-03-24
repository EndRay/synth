package sources.utils;

import sources.AbstractSignalProcessor;
import sources.SignalSource;

public class Socket extends AbstractSignalProcessor {
    public Socket(SignalSource source) {
        super(source);
    }

    public void set(double value){
        setSignalSource(new DC(value));
    }

    public void setFrequency(double frequency){
        setSignalSource(DC.getFrequencyDC(frequency));
    }

    public void modulate(SignalSource modulator){
        setSignalSource(getSignalSource().add(modulator));
    }

    @Override
    public double getSample(int sampleId) {
        return getSourceSample(sampleId);
    }
}
