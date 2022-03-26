package sources.filters;

import sources.SignalSource;
import sources.utils.Socket;

abstract public class AbstractResonantFilter extends AbstractFilter implements ResonantFilter{

    private final Socket resonance = new Socket();

    public AbstractResonantFilter(){}

    public AbstractResonantFilter(double frequency){
        super(frequency);
    }

    public AbstractResonantFilter(SignalSource source) {
        super(source);
    }

    public AbstractResonantFilter(SignalSource source, SignalSource frequencySource) {
        super(source, frequencySource);
    }

    public AbstractResonantFilter(SignalSource source, SignalSource frequencySource, SignalSource resonanceSource) {
        super(source, frequencySource);
        resonance.bind(resonanceSource);
    }

    @Override
    public Socket resonance() {
        return resonance;
    }
}
