package synthesizer.sources.filters;

import synthesizer.sources.AbstractSignalProcessor;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;

abstract public class AbstractFilter extends AbstractSignalProcessor implements Filter {
    private final Socket frequency = new Socket();

    public AbstractFilter(){}

    public AbstractFilter(double frequency){
        frequency().set(frequency);
    }

    public AbstractFilter(SignalSource frequencySource) {
        frequency().bind(frequencySource);
    }

    public AbstractFilter(SignalSource source, SignalSource frequencySource) {
        this(frequencySource);
        source().bind(source);
    }

    @Override
    public Socket frequency(){
        return frequency;
    }
}
