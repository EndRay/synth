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
    public double getSample(int sampleId) {
        return source().getFrequency(sampleId);
    }
}
