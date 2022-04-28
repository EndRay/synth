package synthesizer.sources.effects;

import synthesizer.sources.AbstractSignalProcessor;
import synthesizer.sources.SignalProcessor;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;

abstract public class AbstractDistortion extends AbstractSignalProcessor {

    public AbstractDistortion(){}
    public AbstractDistortion(SignalSource source){
        source().bind(source);
    }

    protected abstract double distortFunction(double x);

    @Override
    public double recalculate(int sampleId) {
        return distortFunction(source().getSample(sampleId));
    }
}
