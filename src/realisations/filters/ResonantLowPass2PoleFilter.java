package realisations.filters;

import sources.SignalSource;
import sources.filters.ResonantFilter;
import sources.utils.Socket;

public class ResonantLowPass2PoleFilter extends AbstractSimpleLowPassFilter implements ResonantFilter {
    private final Socket resonance = new Socket();
    double[] buf;

    {
        buf = new double[2];
        buf[0] = 0;
        buf[1] = 0;
    }

    public ResonantLowPass2PoleFilter(){}

    public ResonantLowPass2PoleFilter(SignalSource source) {
        super(source);
    }

    public ResonantLowPass2PoleFilter(SignalSource source, SignalSource frequencySource) {
        super(source, frequencySource);
    }

    public ResonantLowPass2PoleFilter(SignalSource source, SignalSource frequencySource, SignalSource resonanceSource) {
        super(source, frequencySource);
        resonance.bind(resonanceSource);
    }

    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            double f = getAlpha(sampleId);
            buf[0] = buf[0] + f * (source().getSample(sampleId) - buf[0] + getFeedback(sampleId) * (buf[0] - buf[1]));
            buf[1] = buf[1] + f * (buf[0] - buf[1]);
        }
        return buf[1];
    }

    protected double getFeedback(int sampleId) {
        double q = resonance().getSample(sampleId);
        return q + q / (1.0 - getAlpha(sampleId));
    }

    @Override
    public Socket resonance() {
        return resonance;
    }
}
