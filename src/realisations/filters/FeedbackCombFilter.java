package realisations.filters;

import sources.SignalSource;
import sources.filters.AbstractFilter;
import sources.filters.Filter;
import sources.utils.Socket;

public class FeedbackCombFilter extends AbstractFilter implements Filter {
    final private Socket alpha = new Socket();
    final public int maxDelaySamples = (int) (0.5 * sampleRate);
    private final double[] tape = new double[maxDelaySamples + 1];
    private double lastSample = 0;
    private int ptr = 0;

    {
        for (int i = 0; i < maxDelaySamples; ++i)
            tape[i] = 0;
    }

    public FeedbackCombFilter() {
    }

    public FeedbackCombFilter(double frequency) {
        super(frequency);
    }

    public FeedbackCombFilter(double frequency, double alpha) {
        super(frequency);
        alpha().set(alpha);
    }

    public FeedbackCombFilter(SignalSource frequencySource) {
        super(frequencySource);
    }

    public FeedbackCombFilter(SignalSource frequencySource, SignalSource alphaSource) {
        super(frequencySource);
        alpha().bind(alphaSource);
    }

    public FeedbackCombFilter(SignalSource frequencySource, SignalSource alphaSource, SignalSource source) {
        super(frequencySource, source);
        alpha().bind(alphaSource);
    }

    public Socket alpha() {
        return alpha;
    }

    @Override
    protected double recalculate(int sampleId) {
        ++ptr;
        if (ptr > maxDelaySamples)
            ptr = 0;
        int d = (int) (frequency().getTime(sampleId) * sampleRate);
        int delayedPtr = ptr - d;
        if (delayedPtr < 0)
            delayedPtr += maxDelaySamples;
        tape[ptr] = source().getSample(sampleId) + alpha().getSample(sampleId) * tape[delayedPtr];
        return tape[ptr];
    }
}
