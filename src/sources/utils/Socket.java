package sources.utils;

import sources.AbstractSignalProcessor;
import sources.SignalSource;

public class Socket extends AbstractSignalProcessor {
    public Socket(){
        super();
    }
    public Socket(SignalSource source) {
        super(source);
    }

    public void set(double value){
        bind(new DC(value));
    }

    public void setFrequency(double frequency){
        bind(DC.getFrequencyDC(frequency));
    }

    public double getFrequency(int sampleId){
        return SignalSource.voltageToFrequency(getSample(sampleId));
    }

    public void modulate(SignalSource modulator){
        bind(getSource().add(modulator));
    }

    @Override
    public double getSample(int sampleId) {
        return getSourceSample(sampleId);
    }
}
