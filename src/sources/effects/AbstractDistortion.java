package sources.effects;

import sources.SignalSource;
import sources.utils.Socket;

abstract public class AbstractDistortion extends AbstractEffect{
    final private Socket gain = new Socket(1);

    private double lastSample = 0;

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
    public double getWetSample(int sampleId) {
        return distortFunction(source().getSample(sampleId) * gain().getSample(sampleId));
    }
}
