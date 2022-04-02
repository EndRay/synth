package sources.utils;

import sources.AbstractSignalSource;
import sources.SignalProcessor;
import sources.SignalSource;

public class Socket extends AbstractSignalSource implements PseudoSocket {

    SignalSource source;

    public Socket(){
        source = new DC();
    }
    public Socket(double value){ source = new DC(value); }
    public Socket(SignalSource source) {
        this.source = source;
    }

    public void bind(SignalSource source){
        this.source = source;
    }

    public SignalSource getSource(){
        return source;
    }

    public void modulate(SignalSource modulator){
        source = source.add(modulator);
    }

    public void process(SignalProcessor processor){
        processor.source().bind(source);
        source = processor;
    }

    @Override
    public double getSample(int sampleId) {
        return source.getSample(sampleId);
    }
}
