package synthesizer.sources.utils;

import synthesizer.sources.AbstractSignalProcessor;
import synthesizer.sources.SignalProcessor;
import synthesizer.sources.SignalSource;

public class Inverser extends AbstractSignalProcessor implements SignalProcessor {
    public Inverser(){}

    public Inverser(SignalSource source){
        super(source);
    }

    @Override
    protected double recalculate(int sampleId) {
        return 1/source().getSample(sampleId);
    }
}
