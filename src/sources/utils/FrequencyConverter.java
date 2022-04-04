package sources.utils;

import sources.AbstractSignalProcessor;
import sources.SignalProcessor;
import sources.SignalSource;

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
