package synthesizer.sources.utils;

import synthesizer.sources.AbstractSignalProcessor;
import synthesizer.sources.SignalSource;

public class Attenuator extends AbstractSignalProcessor {

    private final Socket coefficient = new Socket();

    public Attenuator(double coefficient) {
        coefficient().set(coefficient);
    }

    public Attenuator(SignalSource coefficientSource) {
        coefficient().bind(coefficientSource);
    }

    public Attenuator(SignalSource source, SignalSource coefficientSource) {
        super(source);
        coefficient().bind(coefficientSource);
    }

    public Socket coefficient() {
        return coefficient;
    }

    @Override
    protected double recalculate(int sampleId) {
        return source().getSample(sampleId) * coefficient().getSample(sampleId);
    }
}
