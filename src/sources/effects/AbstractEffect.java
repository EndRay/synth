package sources.effects;

import sources.AbstractSignalProcessor;
import sources.SignalSource;
import sources.utils.Socket;

abstract public class AbstractEffect extends AbstractSignalProcessor implements Effect{
    private final Socket wet = new Socket();

    public AbstractEffect(){
        wet().set(1);
    }
    public AbstractEffect(double wet){
        wet().set(wet);
    }
    public AbstractEffect(SignalSource source){
        source().bind(source);
    }

    @Override
    public Socket wet(){
        return wet;
    }

    protected abstract double getWetSample(int sampleId);

    @Override
    protected double recalculate(int sampleId) {
        double wetness = wet().getSample(sampleId);
        double wetSample = getWetSample(sampleId), drySample = source().getSample(sampleId);
        return drySample + (wetSample - drySample) * wetness;
    }
}
