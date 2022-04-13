package synthesizer.sources.utils;

import synthesizer.sources.AbstractSignalProcessor;
import synthesizer.sources.SignalProcessor;
import synthesizer.sources.SignalSource;

public class FrequencyConverter extends AbstractSignalProcessor implements SignalProcessor {

    public FrequencyConverter(){}

    public FrequencyConverter(SignalSource source){
        super(source);
    }


    @Override
    protected double recalculate(int sampleId) {
        return source().getFrequency(sampleId);
    }
}
