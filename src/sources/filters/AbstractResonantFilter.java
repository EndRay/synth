package sources.filters;

import sources.SignalSource;
import sources.utils.Socket;

abstract public class AbstractResonantFilter extends AbstractFilter implements ResonantFilter{

    private final Socket resonance = new Socket();

    public AbstractResonantFilter(){}

    public AbstractResonantFilter(double frequency){
        super(frequency);
    }

    public AbstractResonantFilter(SignalSource frequencySource) {
        super(frequencySource);
    }

    public AbstractResonantFilter(SignalSource frequencySource, SignalSource resonanceSource) {
        super(frequencySource);
        resonance.bind(resonanceSource);
    }

    public AbstractResonantFilter(SignalSource frequencySource, SignalSource resonanceSource, SignalSource source) {
        super(frequencySource, source);
        resonance.bind(resonanceSource);
    }

    @Override
    public Socket resonance() {
        return resonance;
    }
}
