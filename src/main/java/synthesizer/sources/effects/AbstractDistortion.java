package synthesizer.sources.effects;

import synthesizer.sources.AbstractSignalProcessor;
import synthesizer.sources.SignalProcessor;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;

abstract public class AbstractDistortion extends AbstractSignalProcessor {
    final private Socket gain = new Socket(1);

    public AbstractDistortion(){}
    public AbstractDistortion(double gain){
        gain().set(gain);
    }
    public AbstractDistortion(SignalSource source){
        source().bind(source);
    }
    public AbstractDistortion(SignalSource source, double gain){
        source().bind(source);
        gain().set(gain);
    }

    public Socket gain(){
        return gain;
    }

    protected abstract double distortFunction(double x);

    @Override
    public double recalculate(int sampleId) {
        return distortFunction(source().getSample(sampleId) * gain().getSample(sampleId));
    }
}
