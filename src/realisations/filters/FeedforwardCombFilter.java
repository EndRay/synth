package realisations.filters;

import sources.SignalSource;
import sources.filters.AbstractFilter;
import sources.filters.Filter;
import sources.utils.Socket;

public class FeedforwardCombFilter extends AbstractFilter implements Filter {
    final private Socket alpha = new Socket();
    final public int maxDelaySamples = (int) (0.5 * sampleRate);
    private final double[] tape = new double[maxDelaySamples + 1];
    private int ptr = 0;

    {
        for (int i = 0; i < maxDelaySamples; ++i)
            tape[i] = 0;
    }

    public FeedforwardCombFilter() {
    }

    public FeedforwardCombFilter(double frequency) {
        super(frequency);
    }

    public FeedforwardCombFilter(double frequency, double alpha) {
        super(frequency);
        alpha().set(alpha);
    }

    public FeedforwardCombFilter(SignalSource frequencySource) {
        super(frequencySource);
    }

    public FeedforwardCombFilter(SignalSource frequencySource, SignalSource alphaSource) {
        super(frequencySource);
        alpha().bind(alphaSource);
    }

    public FeedforwardCombFilter(SignalSource frequencySource, SignalSource alphaSource, SignalSource source) {
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
        tape[ptr] = source().getSample(sampleId);
        return tape[ptr] + alpha().getSample(sampleId) * tape[delayedPtr];
    }
}
