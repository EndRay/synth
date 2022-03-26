package sources.filters;

import sources.AbstractSignalProcessor;
import sources.SignalSource;
import sources.utils.Socket;

abstract public class AbstractFilter extends AbstractSignalProcessor implements Filter {
    private final Socket frequency = new Socket();

    public AbstractFilter(){}

    public AbstractFilter(double frequency){
        frequency().set(frequency);
    }

    public AbstractFilter(SignalSource source) {
        super(source);
        open();
    }

    public AbstractFilter(SignalSource source, SignalSource frequencySource) {
        this(source);
        frequency().bind(frequencySource);
    }

    @Override
    public Socket frequency(){
        return frequency;
    }

    abstract public double getSample(int sampleId);
}
